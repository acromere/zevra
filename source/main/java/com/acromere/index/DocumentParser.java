package com.acromere.index;

import com.acromere.result.Result;

import java.util.Set;

public interface DocumentParser {

	Result<Set<Hit>> index( Document document );

}
