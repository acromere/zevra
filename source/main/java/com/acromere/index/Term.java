package com.acromere.index;

import java.util.List;

public record Term(String context, String word, int length, List<Integer> coordinates) {}
