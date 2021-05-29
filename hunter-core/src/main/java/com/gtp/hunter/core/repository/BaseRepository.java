package com.gtp.hunter.core.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.gtp.hunter.common.manager.ConnectionManager;
import com.gtp.hunter.core.model.BaseModel;

public abstract class BaseRepository<T extends BaseModel<I>, I> extends ConnectionManager {

	public BaseRepository() {
		super();
	}

	public void multiPersist(T... t) {
		for (T i : t) {
			persist(i);
		}
	}

	public void multiPersist(List<T> t) {
		t.stream().forEach(i -> persist(i));
	}

	public void multiPersist(Set<T> t) {
		t.stream().forEach(i -> persist(i));
	}

	public abstract List<T> listAll();

	public abstract T findById(I i);

	public abstract T findByMetaname(String meta);

	public abstract T findByField(String fld, Object val);

	public abstract List<T> listById(Collection<I> ids);

	public abstract List<T> listByFieldIn(String fld, List<String> val);

	public abstract List<T> listByField(String fld, Object val);

	public abstract T persist(T t);

	public abstract void removeById(I id);

}