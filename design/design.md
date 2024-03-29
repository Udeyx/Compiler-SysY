# 编译器设计

## 参考编译器介绍

我参考了课程组提供的 PL0 编译器代码，有如下心得。

### 总体结构

该编译器从前到后主要分为这几部分：

1. 定义一些编译时需要用到的常量和变量(全局)：符号表长度、最大地址、关键字符号名、中间代码指令等，以及比较重要的存放所有生成的指令的`code`数组和记录`code`中现有元素数量的`cx`。
2. 之后是词法分析的部分，定义为`procedure getsym`。在`getsym`中首先定义了用于读一个字符的`procedure getch`。词法分析具体实现的倒是很套路，判断当前一个字符是字母或者数字来按照变量名、关键词、数字字面量等方式解析。
3. 再之后是语法分析环节。对于每一个语法单元 xxx，都有个`procedure xxx`来解析。比较让我意外的是，这些`procedure`是嵌套起来的。比如`procedure statment`中定义了`procedure expression`，`procedure term`中定义了`procedure term`等，在每个`procedure`中都会先定义必要的子`procedure`再开始自己的定义。此外，在定义各种 Parser 之前，代码中定义了`procedure gen`函数，用于把中间代码加入`code`数组，而在词法分析的过程中也不断调用`gen`，这就说明该编译器是**在语法分析的同时进行代码生成的！！！**
4. 之后是`procedure interpret`，也就是翻译生成目标代码的过程。前面`code`数组中的指令(`lit, opr, lod`)应该是类似与中间代码的东西，而这一步这是把中间代码翻译成目标代码。
5. 最后是`main`，主要内容是声明和初始化一些变量并依次调用`getsym`, `block`, `error`(仅在有错误时调用), `interpret`进行词法分析、语法分析&中间代码生成、错误处理、目标代码生成。

### 接口设计

可能是因为 PL0 文法比价简单，这个编译器设计的就也比较简单，几乎没有什么接口设计，各种信息都是直接开成全局变量方便在不同`procedure`间使用。

### 文件组织

整个编译器只有一个文件，文件里的内容先后是定义词法分析、语法分析&中间代码生成、目标代码生成以及调用这些过程的`main`。文件组织并不复杂。

### 感想

可以看出该编译器应该是一个用于教学演示的编译器，主要目的是传达设计思想，纠结其“全局变量作为接口”、“一个文件解决所有”等问题并没有什么意义。在我看来，想直接参考该编译器设计自己的架构肯定是行不通的，由于 SysY 比 PL0 复杂不少，一边 parse 一边生成中间代码(语法制导翻译)这种耦合性较高的行为很容易导致各种 bug，所以我自己的架构还是采用了多遍的方式。但这份代码确实让我有所收获，一方面让我了解了编译器的大致流程，另一方面也让我见识到了分程序结构的样子。

---

以下是我的编译器的设计。

## 总体设计

总体来看，我的编译器分为前端、中端、后端。前端执行词法分析和语法分析，中端执行中端优化，后端执行寄存器分配等。而中间代码生成和错误处理在前端生成的语法树上执行，目标代码生成在中端的 LLVM Module 上执行。

## 接口设计

以下是`Compiler.java`部分内容：

```java
ArrayList<Token> tokens = new Lexer(source).tokenize();
CompUnit compUnit = new Parser(new Iter(tokens)).parseCompUnit();
compUnit.check();
if (Handler.getInstance().hasError()) {
	IOer.printError();
	return;
}
compUnit.buildIR();
if (Optimizer.getInstance().isDev())
	IOer.printIR();
Optimizer.getInstance().optimize();
Module.getInstance().buildFIFOMIPS();
IOer.printMIPS();
```

- Lexer 和 Parser 的接口是一个用来遍历`token`列表的`Iter`(自制的遍历类)。
- Parse 后生成的 AST 信息包含在树根`compUnit`中，错误处理和中间代码生成分别调用其`check()`和`buildIR()`两个方法。
- 优化器单例类`Optimizer`负责对产生的中间代码进行优化。
- 调用单例 LLVM `MOdule`的`buildFIFOMIPS()`则可生成 MIPS 目标代码

## 文件组织

以下是我项目的文件组织结构，逻辑比较清晰，分为前中后端。前端包括词法分析和语法分析，中端包含中间代码有关类和中端优化`Pass`，后端包含目标代码生成所需的数据结构。

```shell
├── src
│   ├── backend
│   │   ├── directive
│   │   └── instr
│   │       ├── ext
│   │       ├── i
│   │       ├── j
│   │       └── r
│   ├── frontend
│   │   ├── node
│   │   │   ├── decl
│   │   │   ├── exp
│   │   │   └── func
│   │   └── symbol
│   ├── midend
│   │   ├── ir
│   │   │   ├── type
│   │   │   └── value
│   │   │       └── instruction
│   │   └── optimizer
│   └── util
```

## 词法分析

### 编码前设计

我对于 Lexer 有以下几个设计点：

- 先代码去注释再提取`tokens`，去注释分为两步，第一步去掉每一行的单行注释，第二部去掉多行注释（**注意，这种思路是错误的，在编码后进行了更改**）
- 依次读取每一行的字符串生成`ArrayList<Token> tokens`
- 把每一行的`token`合并得到整个文件的`tokens`
- 自定义一个`Iter`类用于遍历`tokens`，具有向后看、回溯等功能

### 编码后修改

- 在编码后的测试中，发现去注释的实现有问题，如果单行注释与多行注释的结尾会导致把该结尾去掉，导致多行注释没有结尾，从而 RE。并且上面的方法其实遍历了多次输入字符串，效率较低。重构后采用了教程的写法，遍历一遍，通过匹配注释和结尾的方法来去注释

## 语法分析

### 编码前设计

- 为每种语法成分建立一个类
- 建立`Parser`类，并在其中为每个 AST 节点建立`parse`方法，递归下降进行语法分析
- 使用前面定义的`Iter`管理`token`流，约定在每个`parse`开局使用`iter.next()`获取下一个`token`或者使用`iter.preview()`前瞻
- 在输出是采用后序遍历的方式遍历语法树，输出语法、词法信息
- 对于包含左递归文法的语法成分（比如`AddExp`，`MulExp`），我会在解析的时候按照非左递归文法解析，但是在构建语法树的时候转换成左递归
- 对于文法中一些匹配次数不确定的情况，比如`for`里面的`forStmt`，我采用判断下一个字符是否是应该在其后的字符来判断，比如是不是`;`或者`)`

### 编码后修改

- 在编码后我对最后一点进行修改，因为在错误处理中可能出现缺少`;`或者`)`的情况，上面的判断方式就用不了了。后面我按照教程的写法改成了判断下一个`token`是否在`forStmt`的 FIRST 集中。

## 错误处理

### 编码前设计

- 设计一个单例`ErrorHandler`类，收集发现的错误，并在输出时按行号对错误排序
- 建立`Symbol`, `SymbolTable`, `SymbolManager`几个类用作符号表管理，`Symbol`代表符号，`SymbolTable`是符号表，而`SymbolManager`中包含符号表以及当前循环深度、当前所处函数等信息
- a 类错误在`Stmt`类中遍历格式化字符串处理
- i, j, k 类在`Parser`中处理，采用尽量预读，必要时回溯的方法
- b, c 通过遍历符号表确定，区别在于 b 只看当前层次的符号表而 c 要看全部符号
- 对于 d,e 错误，建立`FuncSymbol extends Symbol`，在`FuncDef`以及`FuncFParams`中记录函数的参数个数及各位置对应的类型到`FuncSymbol`中，并在解析`FuncRParams`的时候对比
- f, g 错误看似相似但处理方式大有不同。f 错误要在`Stmt`类的`check`方法中处理，依据`SymbolManager`中当前函数符号信息判断返回类型是否正确。而 g 错误则是要在`FuncDef`中抽出`Block`的最后一条`BlockItem`看是否是`Stmt`且是否是`return`
- h 通过在`Stmt`中判断赋值语句的对象是否是`ConSymbol`来检查
- l 在`Stmt`的`check`中检查，与 a 类似
- m 需要在`SymbolManager`中维护当前的循环深度，并在`Stmt`中判断是否处于循环中

### 编码后修改

错误处理部分编码后并没有修改

## 中间代码生成

### 编码前设计

在做设计前，我看了 LLVM 的一些文档，这里先放上学习的笔记，再写自己的设计

#### LLVM 核心类层次结构

##### Type 类及其衍生类

- Type 不能直接实例化，只能通过子类实例化
- 继承了 Type 的基本类有 IntegerType, ArrayType, FunctionType 等
- 每个 Type 对象应该是单例的，因此可以用枚举类
- LLVM 自带一些和 Type 有关的方法：`isIntegerTy`, `isFloatingPointTy`。代表 is ... type

##### Module 类

- 代表了一个被编译文件的顶层结构（对应咱们的 CompUnit），记录一系列的 Function, GlobalVariables 以及**一个 SymbolTable**
- 具有`insertGlobalVariable`, `getSymbolTable`等方法

##### Value 类

- LLVM 中最重要的类
    - 代表了一个**有类型**的值
    - 在各种指令中被当作操作数使用
- 这些都是 Value：Constant, Argument, Function, Instruction
- 一个 Value 会在一个 LLVM 表示中被使用多次。为了维护 Value 和使用者的关系，Value 中维护了一个 User 列表，保存使用它的使用者的信息
- Value 可以有名字，但不是必须的
- 在 LLVM 中，一个 ssa 变量和产生该变量的式子是等价的

##### User 类

- 代表了 llvm 图的结点
- 是 Value 的子类

---

以下是我的设计思想：

- 可以在代码生成中继续用以前的方式维护符号表，并继续用以前的符号
- 把二维数组当作一维数组处理
- 建立一个单例`Module`类作为 LLVM ir 的顶层模块
- 创建各种类`Value, User, Use`以及各种指令，并建立一个`IRBuilder`类统一管理各种类的构造，以及把指令添加到基本块等工作。此外，创建`NameSpace`类管理函数、基本块、变量的命名
- 全局变量
    - 类型全部都是`PointerType`区别在于`elementType`可能是`Integer.I32`或者`ArrayType`
    - 在所有`Exp`和类`Exp`的类(比如`Number, LVal`)中建立编译时求值方法`evaluate`，来实现全局变量的初始值
    - 当全局数组未被初始化时使用`zeroinitializer`
- 局部变量
    - 在做 mem2reg 之前，局部变量也是内存形式的，用`alloca`声明，用`load/store`读写
    - 建立局部变量时要维护符号表
- 函数
    - 采用`FunctionType`类型，在其中存储函数参数和返回值信息
    - 在创建一个函数时先默认加入一个基本块
    - 为形参建立一个`Param`类，记录类型
    - 形参数组传入的类型是`I32*`，而对于`I32`形参，需要先为其`alloca`一个变量作为替代，防止最后修改形参变量时违背 SSA
- 逻辑判断&短路求值
    - 使用`icmp`和`br`来实现条件判断和跳转
    - 在 LLVM 中没法直接使用与或逻辑，因此我把`LOrExp`和`LAndExp`展开成关于`EqExp`的二维`ArrayList`，行中的`EqExp`是与的关系，行与行之间是或的关系。在解析时先遍历每行生成与逻辑再遍历各行生成或逻辑。
- 数组
    - 使用`getelementptr`指令进行取指针操作
    - 对于`[n x i32]*`形式的数组，要有两个偏移量，第一个是 0。对于`i32*`形式的数组(作为函数参数)，只用一个偏移量

### 编译后修改

中间代码生成以后，我并没有对原来的设计思路进行修改，后面的优化过程中也没有改动这部分代码。

## 目标代码生成

### 编写前设计

对于目标代码生成，我有如下设计：

- 为每种 MIPS 指令建一个类
- 建立`MIPSBuilder`类，管理每一种指令的创建以及符号表和已生成指令的维护
- 在每个 LLVM 类(`Function, BasicBlock`以及各个指令)中写`buildMIPS`方法，用于生成中间代码
- 在优化前，不分配全局寄存器，仅使用`k0, k1`充当临时寄存器，实现各种指令
- 为每个变量在内存上开辟一块空间，每当用到该变量时都先访存读数据到临时寄存器中再执行指令
- 在不断使用栈空间时，并不改变`$sp`的值，而是记录当前使用的栈空间大小(相对`$sp`的偏移量)
- 函数调用
    - 在函数调用之前先把`$ra, $sp`压栈，之后把`$sp`移动到此时的栈底，接着压入参数并`jal func`调用函数
    - 调用结束后从栈上`$sp`目前位置+4 的地方`lw`出原来`$sp`的值
    - 在`Function`类`buildMIPS`的开头先按照参数的数量，从`$sp - 4`, `$sp - 8, ...`位置处取出参数并按照参数名维护符号表
    - 在函数返回时，如果函数非`void`要置`$v0`为返回值
- 数组
    - 由于`.data`字段的数组是地址从小到大生成了，局部数据也采用这种策略存储，否则当数组作为参数时，无法判断其生长方向
- 指针
    - 在我的设计中，真正的实现了指针。具体而言，每当使用`alloca`申请一片空间时，都会在空间下方申请一个 4 字节的指针，其值为上述申请空间的最低地址

### 编写后修改

在最初的设计中，我的符号表是`HashMap<String, Integer>`，记录每个变量分配的内存空间相对于`$sp`的偏移。后面发现使用这样的符号表其实不方便，因为每次读写符号表时都需要调用`Value.getName()`方法取得变量名，且意义并不大。因此后面改为采用`HashMap<Value, Integer>`形式的符号表。

## 代码优化

在这里，直接附上我的代码优化文档，里面有详细的设计和实现方法。

在完成编译器基本功能后，我进行了 mem2reg&后端消 phi、寄存器分配、GVN&GCM(包括常量折叠等)、死代码删除、ZExt 删除这几个优化，下面按照我写优化的顺序介绍。

### mem2reg

在我看来 mem2reg 对于使用 LLVM ir 中间代码的编译器来说是必做的优化，不做的话中间代码将存在大量的访存指令，性能非常拉胯，也会降低后面 GVN 等算法的优化效果（GVN 应该处理不了`load, store`指令，因为内存不是 SSA 的）。我是完全按照教程上的进行实现，便不再提教程上已有的部分（几个算法的伪代码等），主要谈谈自己遇到的困难、对应的解决方式以及支撑算法进行的结构设计。

#### 构建 CFG

控制流图是 mem2reg 所需的基本信息之一，包含了各块之间前驱后继关系，表示了数据流。我选择在生成中间代码的过程中维护，具体而言，我为每个基本块增加了装前驱和后继的`HashSet`，并在生成`br`指令时进行维护：

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

这里没有遇到什么困难，有一个细节就是里面会先判断是否添加成功再维护基本块的关系。这是因为我原本生成的中间代码可能包含`br`后还有指令的情况，甚至`br`后还有`br`的情况，这些都是无用的死代码，且多个`br`会导致数据流分析出现错误。因此我在写 mem2reg 的过程中改善了以前的代码，每次向基本块里面加新指令时，都判断当前块是否已经存在`br`，如果有就不加入。

#### 删除不可达基本块

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

`continue`后面循环中的块显然是不可达的，因此可以先行删掉，不删的话会导致执行指导书上计算支配边界的算法 RE。我处理这个问题的做法是顺序遍历基本块，删掉没有前驱的基本块（每个函数中第一个除外），并维护 CFG 中边的关系。

- 这里有一个问题，如果不可达的基本块不是一个，而是一串，遍历一遍可以删完吗？

    答案是可以的，因为一个块的后继在每个函数的基本块列表中肯定在该块之后，删掉该块并维护边的关系后，后继块就没有前驱了，被遍历到时也会被删掉。

#### 计算支配关系

计算支配关系按照指导书上的算法做就行了，即：某基本块的 dom <- 某基本块所有前驱的 dom 的交集加上自己本身。有一个坑点是指导书（似乎？）没有说算法的初始条件，每个基本块（除了入口）的 dominators 在初始时应该是全集。

#### 建立支配树

建立支配树是为了后面进行变量重命名做准备，目标是给出一颗支配树，每个节点的父节点都是其直接支配者。我的做法是用二重循环遍历每个基本块的 dominators 集，其中唯一一个不支配集合内其他基本块的基本块就是直接支配者。

#### 计算支配边界

支配边界的计算也是按照指导书上的伪代码进行即可，有一点 tips 是`HashSet`确实很适合处理集合，`addAll`和`retainAll`非常适合模拟集合的并和交。

#### 插 Phi

这一步我也是按照指导书上的伪代码进行的，感觉没有什么坑点，唯一感觉比较麻烦的是 Java 实现从`HashSet`中任取一个元素这件事似乎只能通过循环加`break`来实现。

#### 变量重命名

变量重命名我感觉是 mem2reg 中最难的一步，我花了大量时间 debug。大致思想如下：

1. 开一个`HashMap<Value, Stack<Value>>`，用来存每个变量的当前定义，key 是`alloca`，value 这是变量的定义栈
2. 每当碰见`store`或者`phi`时更新定义，并删除`store`
3. 碰见`load`时用栈顶定义替换所有对`load`的使用，并删除`load`
4. 遍历完当前基本块的所有指令后，遍历支配树上的子节点
5. 在 visit 完某个块结束前，要把在该块中所有的变量定义都出栈对应的次数，否则可能出现兄弟节点用到了当前节点中定义的情况

我感觉这里最大的坑点就是 5 中的出栈，我之前没有看懂指导书伪代码中倒着的 T 的意思，所以是用`HashMap<Value, Value>`来保存定义的，显然就会出现上面的问题，这个卡了我很久。

### 消 Phi

消 Phi 是加 Phi 后必做的一步，因为 mips 并不能翻译`phi`指令。这里模拟好指导书上的两个伪代码就可以通过，有一个

值得注意的点是消 Phi 应该在所有中端优化结束后做，因为消 Phi 后加入的"move"并不存在于 LLVM。

### 寄存器分配

寄存器分配是我在 mem2reg 后第一时间想做的，因为在 mips 中只用内存和临时寄存器的性能确实不可观。因为时间有限，我没能去写一个图着色等高效算法，只是仿照 OS 中内存管理的方式写了类似 LRU 的分配。由于操作系统是不知道当前时间之后发生的事的，所以 LRU 已经算很好的算法，但编译过程确实可以知道所有信息，因此 LRU 在编译中效果一般（但还是为我带来了 5%~20%的提升）。具体实现思路如下：

1. 在`MIPSBuilder`中维护一个寄存器分配队列，记录每个寄存器都分配给了哪个`Value`
2. 即使给某个变量分配了寄存器，也在内存上为其预留存储空间
3. 当目前有空寄存器时，为一个新的变量申请寄存器，并把这一对分配插入队尾
4. 当使用到一个已经被分配寄存器的`Value`时，把该对从队列中取出并塞到队尾，实现类似 LRU 的效果
5. 当遇到一个新的变量，但是寄存器已满时，抢占队头变量的寄存器，并把队头元素的值写回为其分配的内存地址
6. 在每个基本块结束时，清空分配队列并写回所有变量的值
7. 在函数调用前，清空分配队列并写回

在采用 LRU 之前，我曾使用的时 FIFO 分配方式，也就是从队头取，向队尾加，之所以这么做是因为认为 LLVM 的中间代码具有较好的局部性，一个变量被声明后一般立刻被用到，好久不用就大概没用了，不太会出现隔很远使用的情况。然而后面 GVN&GCM 把这些都打破了，这也导致做完 GVN&GCM 后，我在几个点的性能有所降低。

### GVN&GCM

GVN 和 GCM 也是一个十分有效的优化，我按照指导书上的大致执行，并在这个过程中进行变量折叠。这两个优化可以解决相同表达式重复计算的问题，可以让第 8 个测试点免于 TLE。我大体上按照指导书来实现，但也有以下自己的想法：

1. 在为 GVN 设计 hashCode 时，其实只用把原来各个指令的`toString`去掉`xxx = `的开头就行，而对于加法和乘法这种存在交换律的指令，可以使用`String`类的`compareTo`方法，固定的把大的或者小的放在前面
2. 并非所有指令都可以参与 GVN&GCM，非 SSA 的指令（`load, store`）控制流有关指令（`br, ret`）以及可能产生副作用的函数调用都是需要`pin`住的。这里副作用处理的比较粗糙，具体包括修改全局变量、修改参数数组、进行 IO、调用其他函数，最后一点并不一定导致副作用，但仔细判断实在太麻烦
3. 我为了图简单，没有执行算法中应有的`schedule_Late`步骤，而是直接选择直接拿到可以的最靠上的基本块中，效果其实也不错

### 死代码删除

死代码删除其实没有为我带来非常大的提升，因为 mem2reg 部分不断更新定义的过程已经删除了大量死代码。但我还是做了，具体有删除死代码、删除死函数以及合并基本块这几个。

#### 删除死代码

这一步其实不难，遍历所有指令，找到没有被使用的就行了(`useList`为空)，但也有两个值得关注的点：

1. 什么可以删？并不是所有不被`use`都能删，`store`，带有副作用的`call`等就不能删
2. 按照什么样的顺序遍历？如果完全按照从前到后的顺序遍历，那么如下的情况就只能删除最后一条：

```c
b = a + 1;
c = b + 1;
d = c + 1;
// ......
```

这样效果显然不理想，因此我选择从后向前遍历，这样一次可以删掉所有这样的死代码，这种做法其实利用了基本块内指令一定是从后向前执行的特点。这样也有个问题，就是该删的 phi 指令可能删不掉，因为 phi 使用的`Value`并不一定是它前面的，对此，我们可以遍历所有指令三遍，从后向前、从前向后再从后向前，这样便可以解决问题。

#### 删除死函数

这一步删除没有被`use`的函数（`main`除外），遍历一遍`module`里面的函数列表即可。

#### 合并基本块

由于我在短路求值的过程中为了写起来简单，会进行为条件判断多开一个基本块等操作。导致中间代码中会有一些可以合并的”基本块对“：前面一个基本块之后后面基本块一个后继，后面基本块只有前面基本块一个前驱。这种情况可以通过把前一个基本块除末尾跳转以外所有指令加入后一个基本块的开头并删除前一个基本块来解决。

### ZExt 删除

在 MIPS 中，所有寄存器都是 32 位的，因此 ZExt 在翻译时只相当于一个 move，没有什么实际意义，且会增加开销。因此我在最后删除了所有的 ZExt 指令，具体而言，对于某个`zext`指令，把它在所有`User`中的使用变成被 0 扩展的`Value`的使用。这样改完后的 ir 肯定是不符合 LLVM 规矩，但不影响生成 MIPS 的正确性。
