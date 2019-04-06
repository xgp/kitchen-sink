package com.github.xgp.xml;

import static org.junit.Assert.*;

import com.github.xgp.xml.slurpersupport.GPathResult;
import org.junit.Test;

public class XmlSlurperTest {

  @Test
  public void parseAttributeAndElement() throws Exception {
    String xml = "<root><one a1=\"uno!\"/><two>Some text!</two></root>";
    GPathResult root = new XmlSlurper().parseText(xml);
    assertEquals(root.name(), "root");
    assertEquals(root.e("one").a("a1"), "uno!");
    assertEquals(root.e("one").a("a1").text(), "uno!");
    assertEquals(root.e("two").text(), "Some text!");
  }
}
