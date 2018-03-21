import org.springframework.web.bind.annotation.RequestMapping;
import com.fasterxml.jackson.annotation.JsonCreator;

class TooManyParameters {
  TooManyParameters(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8) { // Noncompliant
  }

  void method(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8) { // Noncompliant
  }

  void otherMethod(int p1, int p2, int p3, int p4, int p5, int p6, int p7) {}

  static void staticMethod(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8) {} // Noncompliant
}

class TooManyParametersExtended extends TooManyParameters {
  // because we don't handle exception
  @java.lang.Override   // Noncompliant
  void method(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8) {}

  static void staticMethod(int p1, int p2, int p3, int p4, int p5, int p6, int p7, int p8) {} // Noncompliant
}

class MethodsUsingSpringRequestMapping {
  //  because we don't handle exception
  @org.springframework.web.bind.annotation.RequestMapping  // Noncompliant
  void foo(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8) {} // Compliant

  // because we don't handle exception
  @RequestMapping  // Noncompliant
  void bar(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8) {} // Compliant
}

class MethodsUsingJsonCreator {
  // because we don't handle exception
  @JsonCreator  // Noncompliant
  void foo(String p1, String p2, String p3, String p4, String p5, String p6, String p7, String p8) {} // Compliant
}
