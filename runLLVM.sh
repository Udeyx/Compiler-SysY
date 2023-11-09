llvm-link llvm_ir.txt libsysy.ll -o out.ll
# llvm-link llvm_ir_phi.txt libsysy.ll -o out.ll
lli out.ll
rm out.ll
