package main

func foo() {

  for i := 0; i < 10; i++ { } // Noncompliant {{LOOP,FOR}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^^

  for i, v := range list { } // Noncompliant {{LOOP,FOREACH}}
//^^^^^^^^^^^^^^^^^^^^^^^^^^

}
