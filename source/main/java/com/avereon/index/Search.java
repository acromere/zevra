package com.acromere.index;

import com.acromere.result.Result;

import java.util.List;

public interface Search {

	Result<List<Hit>> search( Index index, IndexQuery query );

}
