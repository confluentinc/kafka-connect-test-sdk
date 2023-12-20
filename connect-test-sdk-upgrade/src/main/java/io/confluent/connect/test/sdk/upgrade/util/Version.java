/*
 * Copyright [2023 - 2023] Confluent Inc.
 */

package io.confluent.connect.test.sdk.upgrade.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Version {
  public List<String> tags;
  public List<String> otherTags;
  public static final String PATTERN_STR = "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-"
      + "((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-]"
      + "[0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
  public static final Pattern PATTERN = Pattern.compile(PATTERN_STR);

  public Version(String ver) {
    tags = Arrays.stream(ver.split("-")).collect(Collectors.toList());
    if (tags.size() > 1) {
      otherTags = tags.subList(1, tags.size());
    } else {
      otherTags = new ArrayList<>();
    }
    ver = tags.get(0);
    tags = Arrays.stream(ver.split("\\.")).collect(Collectors.toList());
    cleanUpTags();

  }

  public void cleanUpTags() {
    int i = tags.size() - 1;
    while (i >= 0) {
      try {
        Integer.parseInt(tags.get(i));
      } catch (Exception ex) {
        tags.remove(i);
      }
      i--;
    }
  }

  public Boolean isGreaterThan(Version v2) {
    for (int i = 0; i < Math.min(this.tags.size(),v2.tags.size()); i++) {
      int tag1 = Integer.parseInt(this.tags.get(i));
      int tag2 = Integer.parseInt(v2.tags.get(i));
      if (tag1 < tag2) {
        return false;
      } else if (tag1 > tag2) {
        return true;
      }
    }
    return true;
  }

  public int compare(Version v2) {
    if (this.isEquals(v2)) {
      return 0;
    } else if (this.isGreaterThan(v2)) {
      return 1;
    } else {
      return -1;
    }
  }

  public Boolean isSmallerThan(Version v2) {
    return v2.isGreaterThan(this);
  }

  public Boolean isEquals(Version v2) {
    if (this.tags.size() != v2.tags.size()) {
      return false;
    }
    for (int i = 0; i < this.tags.size(); i++) {
      if (Integer.parseInt(this.tags.get(i)) != Integer.parseInt(v2.tags.get(i))) {
        return false;
      }
    }
    return true;
  }

  public boolean evaluate(Version v,String condition) {
    switch (condition) {
      case ">":
        return this.isGreaterThan(v);
      case "<":
        return this.isSmallerThan(v);
      case "=":
        return this.isEquals(v);
      default:
        return true;
    }
  }

  @Override
  public String toString() {
    if (otherTags.isEmpty()) {
      return String.join(".", this.tags);
    }
    return String.join(".", this.tags) + "-" + String.join("-", this.otherTags);
  }
}
