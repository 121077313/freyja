package org.freyja.cache;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.freyja.cache.annotation.CacheDelete;
import org.freyja.cache.annotation.CacheGet;
import org.freyja.cache.annotation.CacheKeys;
import org.freyja.cache.annotation.CacheListAdd;
import org.freyja.cache.annotation.CacheListRemove;
import org.freyja.cache.annotation.CacheListReplace;
import org.freyja.cache.annotation.CacheSave;
import org.freyja.cache.annotation.CacheSet;
import org.freyja.cache.annotation.Caches;
import org.freyja.cache.operation.CacheDeleteOperation;
import org.freyja.cache.operation.CacheGetOperation;
import org.freyja.cache.operation.CacheKeysOperation;
import org.freyja.cache.operation.CacheListAddOperation;
import org.freyja.cache.operation.CacheListRemoveOperation;
import org.freyja.cache.operation.CacheListReplaceOperation;
import org.freyja.cache.operation.CacheSaveOperation;
import org.freyja.cache.operation.CacheSetOperation;
import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.util.ObjectUtils;

public class FreyjaCacheAnnotationParser implements CacheAnnotationParser,
		Serializable {

	private Map<String, Collection<CacheOperation>> map = new ConcurrentHashMap<String, Collection<CacheOperation>>();

	public Collection<CacheOperation> parseCacheAnnotations(AnnotatedElement ae) {

		Collection<CacheOperation> ops = map.get(ae.toString());
		if (ops != null) {
			return ops;
		}
		Collection<CacheDelete> deletes = getAnnotations(ae, CacheDelete.class);
		if (deletes != null) {
			ops = lazyInit(ops);
			for (CacheDelete delete : deletes) {
				ops.add(parseDeleteAnnotation(ae, delete));
			}
		}
		Collection<CacheSave> saves = getAnnotations(ae, CacheSave.class);
		if (saves != null) {
			ops = lazyInit(ops);
			for (CacheSave e : saves) {
				ops.add(parseSaveAnnotation(ae, e));
			}
		}

		Collection<CacheListAdd> adds = getAnnotations(ae, CacheListAdd.class);
		if (adds != null) {
			ops = lazyInit(ops);
			for (CacheListAdd e : adds) {
				ops.add(parseAddAnnotation(ae, e));
			}
		}
		Collection<CacheListRemove> removes = getAnnotations(ae,
				CacheListRemove.class);
		if (removes != null) {
			ops = lazyInit(ops);
			for (CacheListRemove e : removes) {
				ops.add(parseRemoveAnnotation(ae, e));
			}
		}

		Collection<CacheListReplace> replaces = getAnnotations(ae,
				CacheListReplace.class);
		if (replaces != null) {
			ops = lazyInit(ops);
			for (CacheListReplace e : replaces) {
				ops.add(parseReplaceAnnotation(ae, e));
			}
		}

		Collection<Caches> caches = getAnnotations(ae, Caches.class);
		if (caches != null) {
			ops = lazyInit(ops);
			for (Caches c : caches) {
				ops.addAll(parseCachesAnnotation(ae, c));
			}
		}

		Collection<CacheKeys> keys = getAnnotations(ae, CacheKeys.class);
		if (keys != null) {
			ops = lazyInit(ops);
			for (CacheKeys c : keys) {
				ops.add(parseKeysAnnotation(ae, c));
			}
		}
		Collection<CacheGet> get = getAnnotations(ae, CacheGet.class);
		if (get != null) {
			ops = lazyInit(ops);
			for (CacheGet c : get) {
				ops.add(parseGetAnnotation(ae, c));
			}
		}

		Collection<CacheSet> set = getAnnotations(ae, CacheSet.class);
		if (set != null) {
			ops = lazyInit(ops);
			for (CacheSet c : set) {
				ops.add(parseSetAnnotation(ae, c));
			}
		}

		if(ops==null) {
			return ops;
		}
		map.put(ae.toString(), ops);
		return ops;
	}

	private <T extends Annotation> Collection<CacheOperation> lazyInit(
			Collection<CacheOperation> ops) {
		return (ops != null ? ops : new ArrayList<CacheOperation>(1));
	}

	CacheDeleteOperation parseDeleteAnnotation(AnnotatedElement ae,
			CacheDelete caching) {
		CacheDeleteOperation cuo = new CacheDeleteOperation();
		cuo.setCacheNames(caching.value());
		cuo.setCondition(caching.condition());
		cuo.setKey(caching.key());
		cuo.setName(ae.toString());
		cuo.setKeyValueFromReturn(caching.keyValueFromReturn());
		return cuo;
	}

	CacheSaveOperation parseSaveAnnotation(AnnotatedElement ae,
			CacheSave caching) {
		CacheSaveOperation ceo = new CacheSaveOperation();
		ceo.setCacheNames(caching.value());
		ceo.setCondition(caching.condition());
		ceo.setKey(caching.key());
		ceo.setName(ae.toString());
		ceo.setKeyValueFromReturn(caching.keyValueFromReturn());
		return ceo;
	}

	CacheListAddOperation parseAddAnnotation(AnnotatedElement ae,
			CacheListAdd caching) {
		CacheListAddOperation ceo = new CacheListAddOperation();
		ceo.setCacheNames(caching.value());
		ceo.setCondition(caching.condition());
		ceo.setKey(caching.key());
		ceo.setName(ae.toString());
		ceo.setKeyValueFromReturn(caching.keyValueFromReturn());
		return ceo;
	}

	CacheListRemoveOperation parseRemoveAnnotation(AnnotatedElement ae,
			CacheListRemove caching) {
		CacheListRemoveOperation ceo = new CacheListRemoveOperation();
		ceo.setCacheNames(caching.value());
		ceo.setCondition(caching.condition());
		ceo.setKey(caching.key());
		ceo.setName(ae.toString());
		ceo.setKeyValueFromReturn(caching.keyValueFromReturn());
		return ceo;
	}

	CacheListReplaceOperation parseReplaceAnnotation(AnnotatedElement ae,
			CacheListReplace caching) {
		CacheListReplaceOperation ceo = new CacheListReplaceOperation();
		ceo.setCacheNames(caching.value());
		ceo.setCondition(caching.condition());
		ceo.setKey(caching.key());
		ceo.setName(ae.toString());
		ceo.setKeyValueFromReturn(caching.keyValueFromReturn());
		return ceo;
	}

	CacheKeysOperation parseKeysAnnotation(AnnotatedElement ae, CacheKeys keys) {
		CacheKeysOperation ceo = new CacheKeysOperation();
		ceo.setCacheNames(keys.value());
		ceo.setCondition(keys.condition());
		ceo.setName(ae.toString());
		return ceo;
	}

	CacheGetOperation parseGetAnnotation(AnnotatedElement ae, CacheGet get) {
		CacheGetOperation ceo = new CacheGetOperation();
		ceo.setCacheNames(get.value());
		ceo.setCondition(get.condition());
		ceo.setKey(get.key());
		ceo.setName(ae.toString());
		return ceo;
	}

	CacheSetOperation parseSetAnnotation(AnnotatedElement ae, CacheSet set) {
		CacheSetOperation ceo = new CacheSetOperation();
		ceo.setCacheNames(set.value());
		ceo.setCondition(set.condition());
		ceo.setKey(set.key());
		ceo.setName(ae.toString());
		ceo.setKeyValueFromReturn(set.keyValueFromReturn());
		return ceo;
	}

	Collection<CacheOperation> parseCachesAnnotation(AnnotatedElement ae,
			Caches caching) {
		Collection<CacheOperation> ops = null;

		CacheDelete[] deletes = caching.delete();
		if (!ObjectUtils.isEmpty(deletes)) {
			ops = lazyInit(ops);
			for (CacheDelete delete : deletes) {
				ops.add(parseDeleteAnnotation(ae, delete));
			}
		}
		CacheSave[] saves = caching.save();
		if (!ObjectUtils.isEmpty(saves)) {
			ops = lazyInit(ops);
			for (CacheSave save : saves) {
				ops.add(parseSaveAnnotation(ae, save));
			}
		}

		CacheListAdd[] adds = caching.add();
		if (!ObjectUtils.isEmpty(adds)) {
			ops = lazyInit(ops);
			for (CacheListAdd add : adds) {
				ops.add(parseAddAnnotation(ae, add));
			}
		}

		CacheListRemove[] removes = caching.remove();
		if (!ObjectUtils.isEmpty(removes)) {
			ops = lazyInit(ops);
			for (CacheListRemove remove : removes) {
				ops.add(parseRemoveAnnotation(ae, remove));
			}
		}
		CacheListReplace[] replaces = caching.replace();
		if (!ObjectUtils.isEmpty(replaces)) {
			ops = lazyInit(ops);
			for (CacheListReplace replace : replaces) {
				ops.add(parseReplaceAnnotation(ae, replace));
			}
		}

		return ops;
	}

	private static <T extends Annotation> Collection<T> getAnnotations(
			AnnotatedElement ae, Class<T> annotationType) {
		Collection<T> anns = new ArrayList<T>(2);

		// look at raw annotation
		T ann = ae.getAnnotation(annotationType);
		if (ann != null) {
			anns.add(ann);
		}

		// scan meta-annotations
		for (Annotation metaAnn : ae.getAnnotations()) {
			ann = metaAnn.annotationType().getAnnotation(annotationType);
			if (ann != null) {
				anns.add(ann);
			}
		}

		return (anns.isEmpty() ? null : anns);
	}
}
