# 编译器优化文档

在完成编译器基本功能后，我进行了mem2reg&后端消phi、寄存器分配、GVN&GCM(包括常量折叠等)、死代码删除、ZExt删除这几个优化，下面按照我写优化的顺序介绍。

## mem2reg

在我看来mem2reg对于使用LLVM ir中间代码的编译器来说是必做的优化，不做的话中间代码将存在大量的访存指令，性能非常拉胯，也会降低后面GVN等算法的优化效果（GVN应该处理不了`load, store`指令，因为内存不是SSA的）。我是完全按照教程上的进行实现，便不再提教程上已有的部分（几个算法的伪代码等），主要谈谈自己遇到的困难、对应的解决方式以及支撑算法进行的结构设计。

### 构建CFG

控制流图是mem2reg所需的基本信息之一，包含了各块之间前驱后继关系，表示了数据流。我选择在生成中间代码的过程中维护，具体而言，我为每个基本块增加了装前驱和后继的`HashSet`，并在生成`br`指令时进行维护：

```java
public BranchInst buildBranch(Value cond, BasicBlock trueBlock, BasicBlock falseBlock) {
    BranchInst branchInst = new BranchInst(cond, trueBlock, falseBlock);
    boolean added = curBasicBlock.addInst(branchInst);

    if (added) {
        curBasicBlock.addNextBb(trueBlock);
        trueBlock.addPrevBb(curBasicBlock);
        if (!trueBlock.equals(falseBlock)) {
            curBasicBlock.addNextBb(falseBlock);
            falseBlock.addPrevBb(curBasicBlock);
        }
    }
    return branchInst;
}
```

这里没有遇到什么困难，有一个细节就是里面会先判断是否添加成功再维护基本块的关系。这是因为我原本生成的中间代码可能包含`br`后还有指令的情况，甚至`br`后还有`br`的情况，这些都是无用的死代码，且多个`br`会导致数据流分析出现错误。因此我在写mem2reg的过程中改善了以前的代码，每次向基本块里面加新指令时，都判断当前块是否已经存在`br`，如果有就不加入。



### 删除不可达基本块

这是另一个准备工作，在原先的中间代码中可能存在不由起点可达的基本块，这可能因以下情况产生：

```c
int main() {
  // ......
  int i;
  for (i = 0; i < 10; i = i + 1) {
    continue;
    if (1) {
      // do something
    }
  }
	// ......
}

```

`continue`后面循环中的块显然是不可达的，因此可以先行删掉，不删的话会导致执行指导书上计算支配边界的算法RE。我处理这个问题的做法是顺序遍历基本块，删掉没有前驱的基本块（每个函数中第一个除外），并维护CFG中边的关系。

- 这里有一个问题，如果不可达的基本块不是一个，而是一串，遍历一遍可以删完吗？

  答案是可以的，因为一个块的后继在每个函数的基本块列表中肯定在该块之后，删掉该块并维护边的关系后，后继块就没有前驱了，被遍历到时也会被删掉。



### 计算支配关系

计算支配关系按照指导书上的算法做就行了，即：某基本块的dom <- 某基本块所有前驱的dom的交集加上自己本身。有一个坑点是指导书（似乎？）没有说算法的初始条件，每个基本块（除了入口）的dominators在初始时应该是全集。



### 建立支配树

建立支配树是为了后面进行变量重命名做准备，目标是给出一颗支配树，每个节点的父节点都是其直接支配者。我的做法是用二重循环遍历每个基本块的dominators集，其中唯一一个不支配集合内其他基本块的基本块就是直接支配者。



### 计算支配边界

支配边界的计算也是按照指导书上的伪代码进行即可，有一点tips是`HashSet`确实很适合处理集合，`addAll`和`retainAll`非常适合模拟集合的并和交。



### 插Phi

这一步我也是按照指导书上的伪代码进行的，感觉没有什么坑点，唯一感觉比较麻烦的是Java实现从`HashSet`中任取一个元素这件事似乎只能通过循环加`break`来实现。



### 变量重命名

变量重命名我感觉是mem2reg中最难的一步，我花了大量时间debug。大致思想如下：

1. 开一个`HashMap<Value, Stack<Value>>`，用来存每个变量的当前定义，key是`alloca`，value这是变量的定义栈
2. 每当碰见`store`或者`phi`时更新定义，并删除`store`
3. 碰见`load`时用栈顶定义替换所有对`load`的使用，并删除`load`
4. 遍历完当前基本块的所有指令后，遍历支配树上的子节点
5. 在visit完某个块结束前，要把在该块中所有的变量定义都出栈对应的次数，否则可能出现兄弟节点用到了当前节点中定义的情况

我感觉这里最大的坑点就是5中的出栈，我之前没有看懂指导书伪代码中倒着的T的意思，所以是用`HashMap<Value, Value>`来保存定义的，显然就会出现上面的问题，这个卡了我很久。



## 消Phi

消Phi是加Phi后必做的一步，因为mips并不能翻译`phi`指令。这里模拟好指导书上的两个伪代码就可以通过，有一个

值得注意的点是消Phi应该在所有中端优化结束后做，因为消Phi后加入的"move"并不存在于LLVM。



## 寄存器分配

寄存器分配是我在mem2reg后第一时间想做的，因为在mips中只用内存和临时寄存器的性能确实不可观。因为时间有限，我没能去写一个图着色等高效算法，只是仿照OS中内存管理的方式写了类似LRU的分配。由于操作系统是不知道当前时间之后发生的事的，所以LRU已经算很好的算法，但编译过程确实可以知道所有信息，因此LRU在编译中效果一般（但还是为我带来了5%~20%的提升）。具体实现思路如下：

1. 在`MIPSBuilder`中维护一个寄存器分配队列，记录每个寄存器都分配给了哪个`Value`
2. 即使给某个变量分配了寄存器，也在内存上为其预留存储空间
3. 当目前有空寄存器时，为一个新的变量申请寄存器，并把这一对分配插入队尾
4. 当使用到一个已经被分配寄存器的`Value`时，把该对从队列中取出并塞到队尾，实现类似LRU的效果
5. 当遇到一个新的变量，但是寄存器已满时，抢占队头变量的寄存器，并把队头元素的值写回为其分配的内存地址
6. 在每个基本块结束时，清空分配队列并写回所有变量的值
7. 在函数调用前，清空分配队列并写回

在采用LRU之前，我曾使用的时FIFO分配方式，也就是从队头取，向队尾加，之所以这么做是因为认为LLVM的中间代码具有较好的局部性，一个变量被声明后一般立刻被用到，好久不用就大概没用了，不太会出现隔很远使用的情况。然而后面GVN&GCM把这些都打破了，这也导致做完GVN&GCM后，我在几个点的性能有所降低。



## GVN&GCM

GVN和GCM也是一个十分有效的优化，我按照指导书上的大致执行，并在这个过程中进行变量折叠。这两个优化可以解决相同表达式重复计算的问题，可以让第8个测试点免于TLE。我大体上按照指导书来实现，但也有以下自己的想法：

1. 在为GVN设计hashCode时，其实只用把原来各个指令的`toString`去掉`xxx = `的开头就行，而对于加法和乘法这种存在交换律的指令，可以使用`String`类的`compareTo`方法，固定的把大的或者小的放在前面
2. 并非所有指令都可以参与GVN&GCM，非SSA的指令（`load, store`）控制流有关指令（`br, ret`）以及可能产生副作用的函数调用都是需要`pin`住的。这里副作用处理的比较粗糙，具体包括修改全局变量、修改参数数组、进行IO、调用其他函数，最后一点并不一定导致副作用，但仔细判断实在太麻烦
3. 我为了图简单，没有执行算法中应有的`schedule_Late`步骤，而是直接选择直接拿到可以的最靠上的基本块中，效果其实也不错



## 死代码删除

死代码删除其实没有为我带来非常大的提升，因为mem2reg部分不断更新定义的过程已经删除了大量死代码。但我还是做了，具体有删除死代码、删除死函数以及合并基本块这几个。

### 删除死代码

这一步其实不难，遍历所有指令，找到没有被使用的就行了(`useList`为空)，但也有两个值得关注的点：

1. 什么可以删？并不是所有不被`use`都能删，`store`，带有副作用的`call`等就不能删
2. 按照什么样的顺序遍历？如果完全按照从前到后的顺序遍历，那么如下的情况就只能删除最后一条：

```c
b = a + 1;
c = b + 1;
d = c + 1;
// ......
```

这样效果显然不理想，因此我选择从后向前遍历，这样一次可以删掉所有这样的死代码，这种做法其实利用了基本块内指令一定是从后向前执行的特点。这样也有个问题，就是该删的phi指令可能删不掉，因为phi使用的`Value`并不一定是它前面的，对此，我们可以遍历所有指令三遍，从后向前、从前向后再从后向前，这样便可以解决问题。



### 删除死函数

这一步删除没有被`use`的函数（`main`除外），遍历一遍`module`里面的函数列表即可。



### 合并基本块

由于我在短路求值的过程中为了写起来简单，会进行为条件判断多开一个基本块等操作。导致中间代码中会有一些可以合并的”基本块对“：前面一个基本块之后后面基本块一个后继，后面基本块只有前面基本块一个前驱。这种情况可以通过把前一个基本块除末尾跳转以外所有指令加入后一个基本块的开头并删除前一个基本块来解决。



## ZExt删除

在MIPS中，所有寄存器都是32位的，因此ZExt在翻译时只相当于一个move，没有什么实际意义，且会增加开销。因此我在最后删除了所有的ZExt指令，具体而言，对于某个`zext`指令，把它在所有`User`中的使用变成被0扩展的`Value`的使用。这样改完后的ir肯定是不符合LLVM规矩，但不影响生成MIPS的正确性。