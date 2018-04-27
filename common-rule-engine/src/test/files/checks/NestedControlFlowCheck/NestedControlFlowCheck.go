package NestedControlFlowCheck

func nesting(condition1, condition2, condition4, condition5 bool) {
    if condition1 {
//  ^^>
        if condition2 {
//      ^^>
            for i := 1; i <= 10; i++ {
//          ^^^>
                if condition4 {
//              ^^>
                    if condition5 { // Noncompliant {{Refactor this code to not nest more than 4 control flow statements.}}
//                  ^^
     }
     return
    }
   }
  }
 }
}

func nesting2(x int) {
 nums := []int{1, 2, 3}
 switch x {
 case 1:
  for _, i := range nums {
   if i > 1 {
    if i < 3 {
     for j := i; j < 10; j++ { // Noncompliant {{Refactor this code to not nest more than 4 control flow statements.}}

     }
     return
    }
   }
  }
 }
}
