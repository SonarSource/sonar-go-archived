package org.sonar.commonruleengine.rules;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.commonruleengine.Engine;
import org.sonar.commonruleengine.Issue;
import org.sonar.uast.Uast;
import org.sonar.uast.UastNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NoIdenticalFunctionsRuleTest {

  @Test
  void test() {
    UastNode uast = Uast.from(new InputStreamReader(getClass().getResourceAsStream("/uast.json")));
    Engine engine = new Engine(Collections.singletonList(new NoIdenticalFunctionsRule()));
    List<Issue> issues = engine.scan(uast);
    assertEquals(2, issues.size());
    assertEquals(issues.get(0).getMessage(), "Issue here");
    assertEquals(issues.get(0).getNode(), uast.children.get(0).children.get(0));

    assertEquals(issues.get(1).getMessage(), "Issue here");
    assertEquals(issues.get(1).getNode(), uast.children.get(0).children.get(1));
  }
}
