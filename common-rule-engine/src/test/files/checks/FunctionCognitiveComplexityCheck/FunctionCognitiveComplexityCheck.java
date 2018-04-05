class CognitiveComplexityCheck {


  public int ternaryOp(int a, int b) {
    int c = a>b?b:a; // ignore +1, ternary are not yet needed in the usat
    return c>20?4:7; // ignore +1,
  }

  public boolean extraConditions() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}} [[effortToFix=3]]
    return a && b || foo(b && c);
  }
  public boolean extraConditions2() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
    return a && (b || c) || d;
  }
  public void extraConditions3() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}}
    if (a && b || c || d) {}
  }
  public void extraConditions4() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 5 to the 0 allowed.}}
    if (a && b || c && d || e) {}
  }
  public void extraConditions5() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 5 to the 0 allowed.}}
    if (a || b && c || d && e) {}
  }
  public void extraConditions6() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}}
    if (a && b && c || d || e) {}
  }
  public void extraConditions7() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 1 to the 0 allowed.}}
    if (a) {}
  }
  public void extraConditions8() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
    if (a && b && c && d && e) {}
  }
  public void extraConditions9() {// Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
    if (a || b || c || d || e) {}
  }
  public void extraCondition10() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 4 to the 0 allowed.}}
    if (a && b && c || d || e && f){}
  }


  public void switch2(){ // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 12 to the 0 allowed.}}
//            ^^^^^^^
    switch(foo){                              //+1
//  ^^^^^^< {{+1}}
      case 1:
        break;
      case ASSIGNMENT:
        if (lhs.is(Tree.Kind.IDENTIFIER)) {
//      ^^< {{+2 (incl 1 for nesting)}}
          if (                          a && b &&  c || d) {
//        ^^< {{+3 (incl 2 for nesting)}} ^^< {{+1}} ^^< {{+1}}
          }
          if(element.is(Tree.Kind.ASSIGNMENT)) {
//        ^^< {{+3 (incl 2 for nesting)}}
            out.remove(symbol);
          } else {
//          ^^^^< {{+1}}
            out.add(symbol);
          }
        }
        break;
    }
  }

  public void extraCondition11() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
//            ^^^^^^^^^^^^^^^^
    if (a      || (b || c)) {}
//  ^^< {{+1}} ^^< {{+1}}
  }

  public void extraConditions12() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 7 to the 0 allowed.}}
//            ^^^^^^^^^^^^^^^^^
    if (
//  ^^< {{+1}}
      a && b   && c || d || e  && f && g  || (h || (i && j       || k)) || l || m){}
//      ^^< {{+1}}  ^^< {{+1}} ^^< {{+1}} ^^< {{+1}}  ^^< {{+1}} ^^< {{+1}}
  }

  public void breakWithLabel(java.util.Collection<Boolean> objects) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 2 to the 0 allowed.}}
//            ^^^^^^^^^^^^^^
  doABarrelRoll:
    for(Object o : objects) {
//  ^^^< {{+1}}
      break doABarrelRoll;
//    ^^^^^< {{+1}}
    }
  }

  public void doFilter(ServletRequest servletRequest) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 11 to the 0 allowed.}}

    if (consumedByStaticFile) {                             // 1
      return;
    }

    try {

    } catch (HaltException halt) {                          // TODO support +1 for catch

    } catch (Exception generalException) {                  // TODO support +1 for catch

    }

    if (body.notSet() && responseWrapper.isRedirected()) {  // 2
      body.set("");
    }

    if (body.notSet() && hasOtherHandlers) {                // 2
      if (servletRequest instanceof HttpRequestWrapper) {   // 2 (nesting=1)
        ((HttpRequestWrapper) servletRequest).notConsumed(true);
        return;
      }
    }

    if (body.notSet() && !externalContainer) {               // 2
      LOG.info("The requested route [" + uri + "] has not been mapped in Spark");
    }

    if (body.isSet()) {                                      // 1
      body.serializeTo(httpResponse, serializerChain, httpRequest);
    } else if (chain != null) {                              // 1
      chain.doFilter(httpRequest, httpResponse);
    }
  }


  public final T to(U u) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 7 to the 0 allowed.}}

    for (int ctr=0; ctr<args.length; ctr++)
      if (args[ctr].equals("-debug"))
        debug = true ;

    for (int i = chain.length - 1; i >= 0; i--)
      result = chain[i].to(result);

    if (foo)
      for (int i = 0; i < 10; i++)
        doTheThing();

    return (T) result;
  }


  static boolean enforceLimits(BoundTransportAddress boundTransportAddress) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 1 to the 0 allowed.}}
    Iterable<JoinTuple> itr = () -> new JoinTupleIterator(tuples.tuples(), parentIndex, parentReference);

    Predicate<TransportAddress> isLoopbackOrLinkLocalAddress = t -> t.address().getAddress().isLinkLocalAddress()
      || t.address().getAddress().isLoopbackAddress();

  }

  String bulkActivate(Iterator<String> rules) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 4 to the 0 allowed.}}

    try {
      while (rules.hasNext()) {  // +1
        try {
          if (!changes.isEmpty()) {  }  // +2, nesting 1
        } catch (BadRequestException e) { }  // TODO support (+2, nesting 1) for catch
      }
    } finally {
      if (condition) {  // +1
        doTheThing();
      }
    }
    return result;
  }

  private static String getValueToEval( List<String> strings ) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 6 to the 0 allowed.}}
//                      ^^^^^^^^^^^^^^
    if (Measure.Level.ERROR.equals(alertLevel) && foo = YELLOW) {
//  ^^< {{+1}}                                 ^^< {{+1}}
      return condition.getErrorThreshold();
    } else if (Measure.Level.WARN.equals(alertLevel)) {
//    ^^^^< {{+1}}
      return condition.getWarningThreshold();
    } else {
//    ^^^^< {{+1}}
      while (true) {
//    ^^^^^< {{+2 (incl 1 for nesting)}}
        doTheThing();
      }
      throw new IllegalStateException(alertLevel.toString());
    }
  }

  boolean isPalindrome(char [] s, int len) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}}

    if(len < 2)
      return true;
    else
      return s[0] == s[len-1] && isPalindrome(s[1], len-2); // TODO find recursion
  }

  void extraConditions() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 10 to the 0 allowed.}}
//     ^^^^^^^^^^^^^^^
    if (a < b) {
//  ^^< {{+1}}
      doTheThing();
    }

    if (a == b || c > 3 || b-7 == c) {
//  ^^< {{+1}} ^^< {{+1}}
      while (a-- > 0                     && b++ < 10) {
//    ^^^^^< {{+2 (incl 1 for nesting)}} ^^< {{+1}}
        doTheOtherThing();
      }
    }

    do {
//  ^^< {{+1}}
    } while (a-- > 0 || b != YELLOW);
//                   ^^< {{+1}}

    for (int i = 0; i < 10 && j > 20; i++) {
//  ^^^< {{+1}}            ^^< {{+1}}
      doSomethingElse();
    }
  }

  public static void main (String [] args) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 4 to the 0 allowed.}}
//                   ^^^^
    Runnable r = () -> {
      if (condition) {
//    ^^< {{+2 (incl 1 for nesting)}}
        System.out.println("Hello world!");
      }
    };

    r = new MyRunnable();

    r = new Runnable () {
      public void run(){
        if (condition) {
//      ^^< {{+2 (incl 1 for nesting)}}
          System.out.println("Well, hello again");
        }
      }
    };
  }

  int sumOfNonPrimes(int limit) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 8 to the 0 allowed.}}

    int sum = 0;
    OUTER: for (int i = 0; i < limit; ++i) {
      if (i <= 2) {
        continue;
      }
      for (int j = 2; j < 1; ++j) {
        if (i % j == 0) {
          continue OUTER; // TODO support +1 for "continue" to label
        }
      }
      sum += i;
    }
    return sum;
  }

  String getWeight(int i){ // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 4 to the 0 allowed.}}

    if (i <=0) {
      return "no weight";
    }
    if (i < 10) {
      return "light";
    }
    if (i < 20) {
      return "medium";
    }
    if (i < 30) {
      return "heavy";
    }
    return "very heavy";
  }

  public static HighlightingType toProtocolType(TypeOfText textType) { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 1 to the 0 allowed.}}

    switch (textType) {
      case ANNOTATION: {
        return HighlightingType.ANNOTATION;
      }
      case CONSTANT:
        return HighlightingType.CONSTANT;
      case CPP_DOC:
        return HighlightingType.CPP_DOC;
      default:
        throw new IllegalArgumentException(textType.toString());
    }
  }

  public String getSpecifiedByKeysAsCommaList() {
    return getRuleKeysAsString(specifiedBy);
  }

  void localClasses() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 3 to the 0 allowed.}}
//     ^^^^^^^^^^^^
    class local {
      boolean plop() {
        return a && b || c && d;
//               ^^<  ^^<  ^^<
      }
    }
  }

  void noNestingForIfElseIf() { // Noncompliant {{Refactor this function to reduce its Cognitive Complexity from 21 to the 0 allowed.}}
    while (true) { // +1
      if (true) { // +2 (nesting=1)
        for (;;) { // +3 (nesting=2)
          if (true) { // +4 (nesting=3)
          } else if (true) { // +1
          } else { // +1
            if (true) {
            } // +5 (nesting=4)
          }

          if (true) {} // +4 (nesting=3)
        }
      }
    }
  }


}

