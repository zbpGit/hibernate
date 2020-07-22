package info.joyc.tool.collection;

import info.joyc.tool.bean.BeanUtil;
import info.joyc.tool.comparator.PinyinComparator;
import info.joyc.tool.exception.UtilException;
import info.joyc.tool.function.ObjectPropertyPredicate;
import info.joyc.tool.function.PropertyPredicate;
import info.joyc.tool.function.ToBigDecimalFunction;
import info.joyc.tool.lang.Assert;
import info.joyc.tool.lang.Editor;
import info.joyc.tool.lang.Filter;
import info.joyc.tool.lang.Matcher;
import info.joyc.tool.map.MapUtil;
import info.joyc.tool.util.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 集合相关工具类<p>
 * 此工具方法针对{@link Collection}及其实现类封装的工具。<p>
 * 由于{@link Collection} 实现了{@link Iterable}接口，因此部分工具此类不提供，而是在{@link IterUtil} 中提供
 *
 * @author xiaoleilu
 * @see IterUtil
 * @since 3.1.1
 */
public class CollectionUtil {

    static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();

    @SuppressWarnings("unchecked")
    private static <I, R> Function<I, R> castingIdentity() {
        return i -> (R) i;
    }

    /**
     * Simple implementation class for {@code Collector}.
     *
     * @param <T> the type of elements to be collected
     * @param <R> the type of the result
     */
    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner,
                      Function<A, R> finisher, Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner,
                      Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, castingIdentity(), characteristics);
        }

        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }

    /**
     * 集合对象 指定 BigDecimal 属性值求和（一个集合对象中，计算所有对象中指定的某一BigDecimal属性的合计数 ）
     *
     * @param mapper
     * @param <T>
     * @return
     */
    public static <T> Collector<T, ?, BigDecimal> summingBigDecimal(ToBigDecimalFunction<? super T> mapper) {
        return new CollectorImpl<>(() -> new BigDecimal[1], (a, t) -> {
            if (a[0] == null) {
                a[0] = BigDecimal.ZERO;
            }
            a[0] = a[0].add(mapper.applyAsBigDecimal(t));
        }, (a, b) -> {
            a[0] = a[0].add(b[0]);
            return a;
        }, a -> a[0], CH_NOID);
    }

    /**
     * 支持NULL排序 升序
     * 1、null 会排前面
     *
     * @param propertyPredicate 属性
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> sort(ObjectPropertyPredicate<T> propertyPredicate) {
        Comparator<T> comparator = (obj1, obj2) -> {
            Object v1 = propertyPredicate.getProperty(obj1);
            Object v2 = propertyPredicate.getProperty(obj2);
            if (v1 == v2) {
                return 0;
            } else if (v1 == null) {
                return -1;
            } else if (v2 == null) {
                return 1;
            }
            if (v1 instanceof Integer) {
                return (Integer) v1 - (Integer) v2;
            } else if (v1 instanceof Date) {
                return ((Date) v2).compareTo((Date) v1);
            } else if (v1 instanceof BigDecimal) {
                return ((BigDecimal) v2).compareTo((BigDecimal) v1);
            }
            return v1.toString().compareTo(v2.toString());
        };
        return comparator;
    }

    /**
     * 支持NULL排序 降序
     * 1、null 会排后面
     *
     * @param propertyPredicate 属性
     * @param <T>
     * @return
     */
    public static <T> Comparator<T> sortDesc(ObjectPropertyPredicate<T> propertyPredicate) {
        Comparator<T> comparator = (obj1, obj2) -> {
            Object v1 = propertyPredicate.getProperty(obj1);
            Object v2 = propertyPredicate.getProperty(obj2);
            if (v1 == v2) {
                return 0;
            } else if (v1 == null) {
                return 1;
            } else if (v2 == null) {
                return -1;
            }
            if (v1 instanceof Integer) {
                return (Integer) v2 - (Integer) v1;
            } else if (v1 instanceof Date) {
                return ((Date) v2).compareTo((Date) v1);
            } else if (v1 instanceof BigDecimal) {
                return ((BigDecimal) v2).compareTo((BigDecimal) v1);
            }
            return v2.toString().compareTo(v1.toString());
        };
        return comparator;
    }

    /**
     * 利用LinkedHashSet不能添加重复数据并能保证添加顺序的特性，对List去重
     *
     * @param list 集合
     */
    public static <E> List<E> removeDuplicate(List<E> list) {
        LinkedHashSet<E> set = new LinkedHashSet<E>(list.size());
        set.addAll(list);
        return new ArrayList<E>(set);
    }

    /**
     * 集合分组（List）
     *
     * @param allCollections
     * @param predicate
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Map<R, List<T>> groupList(final Collection<T> allCollections, PropertyPredicate<T, R> predicate) {
        return groupCollection(allCollections, predicate, ArrayList::new);
    }

    /**
     * 集合分组（Set）
     *
     * @param allCollections
     * @param predicate
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Map<R, Set<T>> groupSet(final Collection<T> allCollections, PropertyPredicate<T, R> predicate) {
        return groupCollection(allCollections, predicate, HashSet::new);
    }

    /**
     * 集合分组
     *
     * @param allCollections
     * @param predicate
     * @param collectionFactory
     * @param <T>
     * @param <R>
     * @param <C>
     * @return
     */
    public static <T, R, C extends Collection<T>> Map<R, C> groupCollection(final Collection<T> allCollections, PropertyPredicate<T, R> predicate, Supplier<C> collectionFactory) {
        Map<R, C> gc = new HashMap<>();
        allCollections.forEach(t -> (gc.computeIfAbsent(predicate.getProperty(t), k -> collectionFactory.get())).add(t));
        return gc;
    }

    /**
     * 查找叶子数据
     *
     * @param allCollections
     * @param idPredicate
     * @param parentIdPredicate
     * @param <T>
     * @return
     */
    public static <T, R> Set<T> isLeaf(final Collection<T> allCollections, PropertyPredicate<T, R> idPredicate, PropertyPredicate<T, R> parentIdPredicate) {
        if (IterUtil.isNotEmpty(allCollections)) {
            Set<T> ts = new HashSet<>(allCollections);
            ts.removeAll(notLeaf(allCollections, idPredicate, parentIdPredicate));
            return ts;
        }
        return newHashSet();
    }

    /**
     * 查找非叶子数据
     *
     * @param allCollections
     * @param idPredicate
     * @param parentIdPredicate
     * @param <T>
     * @return
     */
    public static <T, R> Set<T> notLeaf(final Collection<T> allCollections, PropertyPredicate<T, R> idPredicate, PropertyPredicate<T, R> parentIdPredicate) {
        if (IterUtil.isNotEmpty(allCollections)) {
            Set<R> parentSet = allCollections.stream().map(parentIdPredicate::getProperty).collect(Collectors.toSet());
            return allCollections.stream().filter(f -> parentSet.contains(idPredicate.getProperty(f))).collect(Collectors.toSet());
        }
        return newHashSet();
    }

    /**
     * 建立指定level层级数据与其下级数据对应关系
     *
     * @param allCollections
     * @param idPredicate
     * @param parentIdPredicate
     * @param <T>
     * @return
     */
    public static <T, R> Map<R, R> levelCorrespondence(final Integer level, final Collection<T> allCollections, PropertyPredicate<T, R> idPredicate, PropertyPredicate<T, R> parentIdPredicate) {
        Assert.notNull(level, "请先指定level层级");
        Map<R, R> relationMap = new HashMap<>();
        Map<R, Integer> layeredMap = layered(allCollections, idPredicate, parentIdPredicate, level);
        Assert.notEmpty(layeredMap, "传入集合未能找到指定level层级：{}", level);
        for (R s : layeredMap.keySet()) {
            Set<R> objects = new HashSet<>();
            objects.add(s);
            Set<R> childSet = loopByIndex(allCollections, objects, parentIdPredicate, idPredicate, HashSet::new);
            relationMap.putAll(childSet.stream().collect(Collectors.toMap(m -> m, s1 -> s)));
        }
        return relationMap;
    }

    /**
     * 根据数据为null为第一层，对数据以关键字段关联关系进行分层输出
     *
     * @param allCollections
     * @param idPredicate
     * @param parentIdPredicate
     * @param needLevel
     * @param <T>
     * @return
     */
    public static <T, R> Map<R, Integer> layered(final Collection<T> allCollections, PropertyPredicate<T, R> idPredicate, PropertyPredicate<T, R> parentIdPredicate, Integer... needLevel) {
        Map<R, Integer> layeredMap = new HashMap<>();
        Map<R, Set<T>> tempGroup = groupSet(allCollections, parentIdPredicate);
        Assert.isTrue(tempGroup.containsKey(null), "传入集合未能找到能作为root的节点");
        Set<R> tempSet = new HashSet<>();
        tempSet.add(null);
        int level = 0;
        while (tempSet.size() > 0) {
            Set<R> currentSet = new HashSet<>();
            Iterator<R> iterator = tempSet.iterator();
            while (iterator.hasNext()) {
                R s = iterator.next();
                if (level > 0) {
                    layeredMap.put(s, level);
                } else {
                    iterator.remove();
                }
                if (tempGroup.containsKey(s)) {
                    currentSet.addAll(tempGroup.get(s).stream().map(idPredicate::getProperty).collect(Collectors.toSet()));
                    tempGroup.remove(s);
                }
            }
            tempSet.addAll(currentSet);
            tempSet.removeAll(layeredMap.keySet());
            if (tempSet.size() == 0 || level > 9) {
                break;
            } else {
                level++;
            }
        }
        if (layeredMap.size() > 0 && needLevel != null && needLevel.length > 0) {
            List<Integer> needLevelList = Arrays.asList(needLevel);
            layeredMap = layeredMap.entrySet().stream().filter(f -> needLevelList.contains(f.getValue())).collect(Collectors.toMap(Entry::getKey, Entry::getValue));
        }
        return layeredMap;
    }

    /**
     * 根据索引集查找关联上下级
     *
     * @param allCollections
     * @param indexCollections
     * @param idPredicate
     * @param parentIdPredicate
     * @param need              数组：第一个元素决定是否包含索引集；第二个元素决定是否查下级；第三个元素决定是否查上级。默认为{true, true, false}
     * @param <T>
     * @return
     */
    public static <T, R, C extends Collection<T>> C findLink(final Collection<T> allCollections, final Collection<R> indexCollections, PropertyPredicate<T, R> idPredicate, PropertyPredicate<T, R> parentIdPredicate, Supplier<C> collectionFactory, boolean... need) {
        Assert.notNull(idPredicate, "请指定id列");
        Assert.notNull(parentIdPredicate, "请指定parentId列");
        boolean[] defaultValues = new boolean[]{true, true, false};
        if (ArrayUtil.isNotEmpty(need)) {
            for (int i = need.length; i > 0; i--) {
                if (i <= defaultValues.length) {
                    defaultValues[i - 1] = need[i - 1];
                }
            }
        }
        if (IterUtil.isNotEmpty(allCollections) && IterUtil.isNotEmpty(indexCollections)) {
            Set<R> idSet = new HashSet<>();
            C rootSet = allCollections.stream().filter(f -> indexCollections.contains(idPredicate.getProperty(f))).collect(Collectors.toCollection(collectionFactory));
            Set<R> rootIdSet = rootSet.stream().map(idPredicate::getProperty).collect(Collectors.toSet());
            Assert.notEmpty(rootSet, "总数据集中无此[{}]的索引数据", StringUtil.join("|", indexCollections));
            if (defaultValues[0]) {
                idSet.addAll(rootIdSet);
            }
            if (defaultValues[1]) {
                // 查找所有子级
                idSet.addAll(loopByIndex(allCollections, rootIdSet, parentIdPredicate, idPredicate, HashSet::new));
            }
            if (defaultValues[2]) {
                // 查找所有父级
                idSet.addAll(loopByIndex(allCollections, rootIdSet, idPredicate, parentIdPredicate, HashSet::new));
            }
            return allCollections.stream().filter(f -> idSet.contains(idPredicate.getProperty(f))).collect(Collectors.toCollection(collectionFactory));
        }
        return collectionFactory.get();
    }

    /**
     * 循环查找关联
     *
     * @param allCollections
     * @param indexCollections
     * @param currentPredicate
     * @param nextPredicate
     * @param <T>
     * @return
     */
    public static <T, R, C extends Collection<R>> C loopByIndex(final Collection<T> allCollections, final Collection<R> indexCollections, PropertyPredicate<T, R> currentPredicate, PropertyPredicate<T, R> nextPredicate, Supplier<C> collectionFactory) {
        C idSet = collectionFactory.get();
        C tempSet = collectionFactory.get();
        tempSet.addAll(indexCollections);
        int loop = 0;
        while (tempSet.size() > 0) {
            Set<R> collect = allCollections.stream().filter(f -> tempSet.contains(currentPredicate.getProperty(f))).map(nextPredicate::getProperty).collect(Collectors.toSet());
            if (collect.size() == 0 || loop > 9) {
                break;
            } else {
                loop++;
                tempSet.clear();
                tempSet.addAll(collect);
                tempSet.removeAll(idSet);
                idSet.addAll(tempSet);
            }
        }
        return idSet;
    }

    // ----------------------------------------------------------------------------------------------

    /**
     * 多个集合的并集<br>
     * 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最多的个数<br>
     * 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c]<br>
     * 结果：[a, b, c, c, c]，此结果中只保留了三个c
     *
     * @param <T>        集合元素类型
     * @param coll1      集合1
     * @param coll2      集合2
     * @param otherColls 其它集合
     * @return 并集的集合，返回 {@link ArrayList}
     */
    @SafeVarargs
    public static <T> Collection<T> union(final Collection<T> coll1, final Collection<T> coll2, final Collection<T>... otherColls) {
        Collection<T> union = union(coll1, coll2);
        for (Collection<T> coll : otherColls) {
            union = union(union, coll);
        }
        return union;
    }

    /**
     * 多个集合的交集<br>
     * 针对一个集合中存在多个相同元素的情况，计算两个集合中此元素的个数，保留最少的个数<br>
     * 例如：集合1：[a, b, c, c, c]，集合2：[a, b, c, c]<br>
     * 结果：[a, b, c, c]，此结果中只保留了两个c
     *
     * @param <T>        集合元素类型
     * @param coll1      集合1
     * @param coll2      集合2
     * @param otherColls 其它集合
     * @return 并集的集合，返回 {@link ArrayList}
     */
    @SafeVarargs
    public static <T> Collection<T> intersection(final Collection<T> coll1, final Collection<T> coll2, final Collection<T>... otherColls) {
        Collection<T> intersection = intersection(coll1, coll2);
        if (isEmpty(intersection)) {
            return intersection;
        }
        for (Collection<T> coll : otherColls) {
            intersection = intersection(intersection, coll);
            if (isEmpty(intersection)) {
                return intersection;
            }
        }
        return intersection;
    }

    /**
     * 判断指定集合是否包含指定值，如果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     *
     * @param collection 集合
     * @param value      需要查找的值
     * @return 如果集合为空（null或者空），返回{@code false}，否则找到元素返回{@code true}
     * @since 4.1.10
     */
    public static boolean contains(final Collection<?> collection, Object value) {
        return isNotEmpty(collection) && collection.contains(value);
    }

    /**
     * 其中一个集合在另一个集合中是否至少包含一个元素，既是两个集合是否至少有一个共同的元素
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @return 其中一个集合在另一个集合中是否至少包含一个元素
     * @see #intersection
     * @since 2.1
     */
    public static boolean containsAny(final Collection<?> coll1, final Collection<?> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return false;
        }
        if (coll1.size() < coll2.size()) {
            for (Object object : coll1) {
                if (coll2.contains(object)) {
                    return true;
                }
            }
        } else {
            for (Object object : coll2) {
                if (coll1.contains(object)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据集合返回一个元素计数的 {@link Map}<br>
     * 所谓元素计数就是假如这个集合中某个元素出现了n次，那将这个元素做为key，n做为value<br>
     * 例如：[a,b,c,c,c] 得到：<br>
     * a: 1<br>
     * b: 1<br>
     * c: 3<br>
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link Map}
     * @see IterUtil#countMap(Iterable)
     */
    public static <T> Map<T, Integer> countMap(Iterable<T> collection) {
        return IterUtil.countMap(collection);
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串<br>
     * 如果集合元素为数组、{@link Iterable}或{@link Iterator}，则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterable    {@link Iterable}
     * @param conjunction 分隔符
     * @return 连接后的字符串
     * @see IterUtil#join(Iterable, CharSequence)
     */
    public static <T> String join(Iterable<T> iterable, CharSequence conjunction) {
        return IterUtil.join(iterable, conjunction);
    }

    /**
     * 以 conjunction 为分隔符将集合转换为字符串<br>
     * 如果集合元素为数组、{@link Iterable}或{@link Iterator}，则递归组合其为字符串
     *
     * @param <T>         集合元素类型
     * @param iterator    集合
     * @param conjunction 分隔符
     * @return 连接后的字符串
     * @see IterUtil#join(Iterator, CharSequence)
     */
    public static <T> String join(Iterator<T> iterator, CharSequence conjunction) {
        return IterUtil.join(iterator, conjunction);
    }

    /**
     * 切取部分数据<br>
     * 切取后的栈将减少这些元素
     *
     * @param <T>             集合元素类型
     * @param surplusAlaDatas 原数据
     * @param partSize        每部分数据的长度
     * @return 切取出的数据或null
     */
    public static <T> List<T> popPart(Stack<T> surplusAlaDatas, int partSize) {
        if (isEmpty(surplusAlaDatas)) {
            return null;
        }

        final List<T> currentAlaDatas = new ArrayList<>();
        int size = surplusAlaDatas.size();
        // 切割
        if (size > partSize) {
            for (int i = 0; i < partSize; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        } else {
            for (int i = 0; i < size; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        }
        return currentAlaDatas;
    }

    /**
     * 切取部分数据<br>
     * 切取后的栈将减少这些元素
     *
     * @param <T>             集合元素类型
     * @param surplusAlaDatas 原数据
     * @param partSize        每部分数据的长度
     * @return 切取出的数据或null
     */
    public static <T> List<T> popPart(Deque<T> surplusAlaDatas, int partSize) {
        if (isEmpty(surplusAlaDatas)) {
            return null;
        }

        final List<T> currentAlaDatas = new ArrayList<>();
        int size = surplusAlaDatas.size();
        // 切割
        if (size > partSize) {
            for (int i = 0; i < partSize; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        } else {
            for (int i = 0; i < size; i++) {
                currentAlaDatas.add(surplusAlaDatas.pop());
            }
        }
        return currentAlaDatas;
    }

    // ----------------------------------------------------------------------------------------------- new HashMap

    /**
     * 新建一个HashMap
     *
     * @param <K> Key类型
     * @param <V> Value类型
     * @return HashMap对象
     * @see MapUtil#newHashMap()
     */
    public static <K, V> HashMap<K, V> newHashMap() {
        return MapUtil.newHashMap();
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>     Key类型
     * @param <V>     Value类型
     * @param size    初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75
     * @param isOrder Map的Key是否有序，有序返回 {@link LinkedHashMap}，否则返回 {@link HashMap}
     * @return HashMap对象
     * @see MapUtil#newHashMap(int, boolean)
     * @since 3.0.4
     */
    public static <K, V> HashMap<K, V> newHashMap(int size, boolean isOrder) {
        return MapUtil.newHashMap(size, isOrder);
    }

    /**
     * 新建一个HashMap
     *
     * @param <K>  Key类型
     * @param <V>  Value类型
     * @param size 初始大小，由于默认负载因子0.75，传入的size会实际初始大小为size / 0.75
     * @return HashMap对象
     * @see MapUtil#newHashMap(int)
     */
    public static <K, V> HashMap<K, V> newHashMap(int size) {
        return MapUtil.newHashMap(size);
    }

    // ----------------------------------------------------------------------------------------------- new HashSet

    /**
     * 新建一个HashSet
     *
     * @param <T> 集合元素类型
     * @param ts  元素数组
     * @return HashSet对象
     */
    @SafeVarargs
    public static <T> HashSet<T> newHashSet(T... ts) {
        return newHashSet(false, ts);
    }

    /**
     * 新建一个LinkedHashSet
     *
     * @param <T> 集合元素类型
     * @param ts  元素数组
     * @return HashSet对象
     * @since 4.1.10
     */
    @SafeVarargs
    public static <T> LinkedHashSet<T> newLinkedHashSet(T... ts) {
        return (LinkedHashSet<T>) newHashSet(true, ts);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>      集合元素类型
     * @param isSorted 是否有序，有序返回 {@link LinkedHashSet}，否则返回 {@link HashSet}
     * @param ts       元素数组
     * @return HashSet对象
     */
    @SafeVarargs
    public static <T> HashSet<T> newHashSet(boolean isSorted, T... ts) {
        if (null == ts) {
            return isSorted ? new LinkedHashSet<T>() : new HashSet<T>();
        }
        int initialCapacity = Math.max((int) (ts.length / .75f) + 1, 16);
        HashSet<T> set = isSorted ? new LinkedHashSet<T>(initialCapacity) : new HashSet<T>(initialCapacity);
        for (T t : ts) {
            set.add(t);
        }
        return set;
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(Collection<T> collection) {
        return newHashSet(false, collection);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>        集合元素类型
     * @param isSorted   是否有序，有序返回 {@link LinkedHashSet}，否则返回{@link HashSet}
     * @param collection 集合，用于初始化Set
     * @return HashSet对象
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Collection<T> collection) {
        return isSorted ? new LinkedHashSet<T>(collection) : new HashSet<T>(collection);
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>      集合元素类型
     * @param isSorted 是否有序，有序返回 {@link LinkedHashSet}，否则返回{@link HashSet}
     * @param iter     {@link Iterator}
     * @return HashSet对象
     * @since 3.0.8
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Iterator<T> iter) {
        if (null == iter) {
            return newHashSet(isSorted, (T[]) null);
        }
        final HashSet<T> set = isSorted ? new LinkedHashSet<T>() : new HashSet<T>();
        while (iter.hasNext()) {
            set.add(iter.next());
        }
        return set;
    }

    /**
     * 新建一个HashSet
     *
     * @param <T>        集合元素类型
     * @param isSorted   是否有序，有序返回 {@link LinkedHashSet}，否则返回{@link HashSet}
     * @param enumration {@link Enumeration}
     * @return HashSet对象
     * @since 3.0.8
     */
    public static <T> HashSet<T> newHashSet(boolean isSorted, Enumeration<T> enumration) {
        if (null == enumration) {
            return newHashSet(isSorted, (T[]) null);
        }
        final HashSet<T> set = isSorted ? new LinkedHashSet<T>() : new HashSet<T>();
        while (enumration.hasMoreElements()) {
            set.add(enumration.nextElement());
        }
        return set;
    }

    // ----------------------------------------------------------------------------------------------- List

    /**
     * 新建一个空List
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @return List对象
     * @since 4.1.2
     */
    public static <T> List<T> list(boolean isLinked) {
        return isLinked ? new LinkedList<T>() : new ArrayList<T>();
    }

    /**
     * 新建一个List
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param values   数组
     * @return List对象
     * @since 4.1.2
     */
    @SafeVarargs
    public static <T> List<T> list(boolean isLinked, T... values) {
        if (ArrayUtil.isEmpty(values)) {
            return list(isLinked);
        }
        List<T> arrayList = isLinked ? new LinkedList<T>() : new ArrayList<T>(values.length);
        for (T t : values) {
            arrayList.add(t);
        }
        return arrayList;
    }

    /**
     * 新建一个List
     *
     * @param <T>        集合元素类型
     * @param isLinked   是否新建LinkedList
     * @param collection 集合
     * @return List对象
     * @since 4.1.2
     */
    public static <T> List<T> list(boolean isLinked, Collection<T> collection) {
        if (null == collection) {
            return list(isLinked);
        }
        return isLinked ? new LinkedList<T>(collection) : new ArrayList<T>(collection);
    }

    /**
     * 新建一个List<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iterable {@link Iterable}
     * @return List对象
     * @since 4.1.2
     */
    public static <T> List<T> list(boolean isLinked, Iterable<T> iterable) {
        if (null == iterable) {
            return list(isLinked);
        }
        return list(isLinked, iterable.iterator());
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param isLinked 是否新建LinkedList
     * @param iter     {@link Iterator}
     * @return ArrayList对象
     * @since 4.1.2
     */
    public static <T> List<T> list(boolean isLinked, Iterator<T> iter) {
        final List<T> list = list(isLinked);
        if (null != iter) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }

    /**
     * 新建一个List<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>        集合元素类型
     * @param isLinked   是否新建LinkedList
     * @param enumration {@link Enumeration}
     * @return ArrayList对象
     * @since 3.0.8
     */
    public static <T> List<T> list(boolean isLinked, Enumeration<T> enumration) {
        final List<T> list = list(isLinked);
        if (null != enumration) {
            while (enumration.hasMoreElements()) {
                list.add(enumration.nextElement());
            }
        }
        return list;
    }

    /**
     * 新建一个ArrayList
     *
     * @param <T>    集合元素类型
     * @param values 数组
     * @return ArrayList对象
     */
    @SafeVarargs
    public static <T> ArrayList<T> newArrayList(T... values) {
        return (ArrayList<T>) list(false, values);
    }

    /**
     * 数组转为ArrayList
     *
     * @param <T>    集合元素类型
     * @param values 数组
     * @return ArrayList对象
     * @since 4.0.11
     */
    @SafeVarargs
    public static <T> ArrayList<T> toList(T... values) {
        return newArrayList(values);
    }

    /**
     * 新建一个ArrayList
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return ArrayList对象
     */
    public static <T> ArrayList<T> newArrayList(Collection<T> collection) {
        return (ArrayList<T>) list(false, collection);
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return ArrayList对象
     * @since 3.1.0
     */
    public static <T> ArrayList<T> newArrayList(Iterable<T> iterable) {
        return (ArrayList<T>) list(false, iterable);
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>  集合元素类型
     * @param iter {@link Iterator}
     * @return ArrayList对象
     * @since 3.0.8
     */
    public static <T> ArrayList<T> newArrayList(Iterator<T> iter) {
        return (ArrayList<T>) list(false, iter);
    }

    /**
     * 新建一个ArrayList<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>        集合元素类型
     * @param enumration {@link Enumeration}
     * @return ArrayList对象
     * @since 3.0.8
     */
    public static <T> ArrayList<T> newArrayList(Enumeration<T> enumration) {
        return (ArrayList<T>) list(false, enumration);
    }

    // ----------------------------------------------------------------------new LinkedList

    /**
     * 新建LinkedList
     *
     * @param values 数组
     * @param <T>    类型
     * @return LinkedList
     * @since 4.1.2
     */
    @SafeVarargs
    public static <T> LinkedList<T> newLinkedList(T... values) {
        return (LinkedList<T>) list(true, values);
    }

    /**
     * 新建一个CopyOnWriteArrayList
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link CopyOnWriteArrayList}
     */
    public static <T> CopyOnWriteArrayList<T> newCopyOnWriteArrayList(Collection<T> collection) {
        return (null == collection) ? (new CopyOnWriteArrayList<T>()) : (new CopyOnWriteArrayList<T>(collection));
    }

    /**
     * 新建{@link BlockingQueue}<br>
     * 在队列为空时，获取元素的线程会等待队列变为非空。当队列满时，存储元素的线程会等待队列可用。
     *
     * @param capacity 容量
     * @param isLinked 是否为链表形式
     * @return {@link BlockingQueue}
     * @since 3.3.0
     */
    public static <T> BlockingQueue<T> newBlockingQueue(int capacity, boolean isLinked) {
        BlockingQueue<T> queue;
        if (isLinked) {
            queue = new LinkedBlockingDeque<>(capacity);
        } else {
            queue = new ArrayBlockingQueue<>(capacity);
        }
        return queue;
    }

    /**
     * 创建新的集合对象
     *
     * @param <T>            集合类型
     * @param collectionType 集合类型
     * @return 集合类型对应的实例
     * @since 3.0.8
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> Collection<T> create(Class<?> collectionType) {
        Collection<T> list = null;
        if (collectionType.isAssignableFrom(AbstractCollection.class)) {
            // 抽象集合默认使用ArrayList
            list = new ArrayList<>();
        }

        // Set
        else if (collectionType.isAssignableFrom(HashSet.class)) {
            list = new HashSet<>();
        } else if (collectionType.isAssignableFrom(LinkedHashSet.class)) {
            list = new LinkedHashSet<>();
        } else if (collectionType.isAssignableFrom(TreeSet.class)) {
            list = new TreeSet<>();
        } else if (collectionType.isAssignableFrom(EnumSet.class)) {
            list = (Collection<T>) EnumSet.noneOf((Class<Enum>) ClassUtil.getTypeArgument(collectionType));
        }

        // List
        else if (collectionType.isAssignableFrom(ArrayList.class)) {
            list = new ArrayList<>();
        } else if (collectionType.isAssignableFrom(LinkedList.class)) {
            list = new LinkedList<>();
        }

        // Others，直接实例化
        else {
            try {
                list = (Collection<T>) ReflectUtil.newInstance(collectionType);
            } catch (Exception e) {
                throw new UtilException(e);
            }
        }
        return list;
    }

    /**
     * 创建Map<br>
     * 传入抽象Map{@link AbstractMap}和{@link Map}类将默认创建{@link HashMap}
     *
     * @param <K>     map键类型
     * @param <V>     map值类型
     * @param mapType map类型
     * @return {@link Map}实例
     * @see MapUtil#createMap(Class)
     */
    public static <K, V> Map<K, V> createMap(Class<?> mapType) {
        return MapUtil.createMap(mapType);
    }

    /**
     * 去重集合
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @return {@link ArrayList}
     */
    public static <T> ArrayList<T> distinct(Collection<T> collection) {
        if (isEmpty(collection)) {
            return new ArrayList<>();
        } else if (collection instanceof Set) {
            return new ArrayList<>(collection);
        } else {
            return new ArrayList<>(new LinkedHashSet<>(collection));
        }
    }

    /**
     * 截取集合的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置（包含）
     * @param end   结束位置（不包含）
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     */
    public static <T> List<T> sub(List<T> list, int start, int end) {
        return sub(list, start, end, 1);
    }

    /**
     * 截取集合的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置（包含）
     * @param end   结束位置（不包含）
     * @param step  步进
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     * @since 4.0.6
     */
    public static <T> List<T> sub(List<T> list, int start, int end, int step) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        final int size = list.size();
        if (start < 0) {
            start += size;
        }
        if (end < 0) {
            end += size;
        }
        if (start == size) {
            return new ArrayList<>(0);
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (end > size) {
            if (start >= size) {
                return new ArrayList<>(0);
            }
            end = size;
        }

        if (step <= 1) {
            return list.subList(start, end);
        }

        final List<T> result = new ArrayList<>();
        for (int i = start; i < end; i += step) {
            result.add(list.get(i));
        }
        return result;
    }

    /**
     * 截取集合的部分
     *
     * @param <T>        集合元素类型
     * @param collection 被截取的数组
     * @param start      开始位置（包含）
     * @param end        结束位置（不包含）
     * @return 截取后的数组，当开始位置超过最大时，返回null
     */
    public static <T> List<T> sub(Collection<T> collection, int start, int end) {
        return sub(collection, start, end, 1);
    }

    /**
     * 截取集合的部分
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置（包含）
     * @param end   结束位置（不包含）
     * @param step  步进
     * @return 截取后的数组，当开始位置超过最大时，返回空集合
     * @since 4.0.6
     */
    public static <T> List<T> sub(Collection<T> list, int start, int end, int step) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        return sub(new ArrayList<T>(list), start, end, step);
    }

    /**
     * 对集合按照指定长度分段，每一个段为单独的集合，返回这个集合的列表
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param size       每个段的长度
     * @return 分段列表
     */
    public static <T> List<List<T>> split(Collection<T> collection, int size) {
        final List<List<T>> result = new ArrayList<>();

        ArrayList<T> subList = new ArrayList<>(size);
        for (T t : collection) {
            if (subList.size() >= size) {
                result.add(subList);
                subList = new ArrayList<>(size);
            }
            subList.add(t);
        }
        result.add(subList);
        return result;
    }

    /**
     * 过滤<br>
     * 过滤过程通过传入的Editor实现来返回需要的元素内容，这个Editor实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，如果返回null表示这个元素对象抛弃
     * 2、修改元素对象，返回集合中为修改后的对象
     * </pre>
     *
     * @param <T>    集合元素类型
     * @param list   集合
     * @param editor 编辑器接口
     * @return 过滤后的数组
     * @since 4.1.8
     */
    public static <T> List<T> filter(List<T> list, Editor<T> editor) {
        final List<T> list2 = (list instanceof LinkedList) ? new LinkedList<T>() : new ArrayList<T>(list.size());
        T modified;
        for (T t : list) {
            modified = editor.edit(t);
            if (null != modified) {
                list2.add(modified);
            }
        }
        return list2;
    }

    /**
     * 过滤<br>
     * 过滤过程通过传入的Filter实现来过滤返回需要的元素内容，这个Filter实现可以实现以下功能：
     *
     * <pre>
     * 1、过滤出需要的对象，{@link Filter#accept(Object)}方法返回true的对象将被加入结果集合中
     * </pre>
     *
     * @param <T>    集合元素类型
     * @param list   集合
     * @param filter 过滤器
     * @return 过滤后的数组
     * @since 4.1.8
     */
    public static <T> List<T> filter(List<T> list, Filter<T> filter) {
        final List<T> list2 = (list instanceof LinkedList) ? new LinkedList<T>() : new ArrayList<T>(list.size());
        for (T t : list) {
            if (filter.accept(t)) {
                list2.add(t);
            }
        }
        return list2;
    }

    /**
     * 去掉集合中的多个元素
     *
     * @param collection  集合
     * @param elesRemoved 被去掉的元素数组
     * @return 原集合
     * @since 4.1.0
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> removeAny(Collection<T> collection, T... elesRemoved) {
        collection.removeAll(newHashSet(elesRemoved));
        return collection;
    }

    /**
     * 通过Editor抽取集合元素中的某些值返回为新列表<br>
     * 例如提供的是一个Bean列表，通过Editor接口实现获取某个字段值，返回这个字段值组成的新列表
     *
     * @param collection 原集合
     * @param editor     编辑器
     * @return 抽取后的新列表
     */
    public static List<Object> extract(Iterable<?> collection, Editor<Object> editor) {
        final List<Object> fieldValueList = new ArrayList<>();
        for (Object bean : collection) {
            fieldValueList.add(editor.edit(bean));
        }
        return fieldValueList;
    }

    /**
     * 获取给定Bean列表中指定字段名对应字段值的列表<br>
     * 列表元素支持Bean与Map
     *
     * @param collection Bean集合或Map集合
     * @param fieldName  字段名或map的键
     * @return 字段值列表
     * @since 3.1.0
     */
    public static List<Object> getFieldValues(Iterable<?> collection, final String fieldName) {
        return extract(collection, new Editor<Object>() {
            @Override
            public Object edit(Object bean) {
                if (bean instanceof Map) {
                    return ((Map<?, ?>) bean).get(fieldName);
                } else {
                    return ReflectUtil.getFieldValue(bean, fieldName);
                }
            }
        });
    }

    /**
     * 查找第一个匹配元素对象
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param filter     过滤器，满足过滤条件的第一个元素将被返回
     * @return 满足过滤条件的第一个元素
     * @since 3.1.0
     */
    public static <T> T findOne(Iterable<T> collection, Filter<T> filter) {
        if (null != collection) {
            for (T t : collection) {
                if (filter.accept(t)) {
                    return t;
                }
            }
        }
        return null;
    }

    /**
     * 查找第一个匹配元素对象<br>
     * 如果集合元素是Map，则比对键和值是否相同，相同则返回<br>
     * 如果为普通Bean，则通过反射比对元素字段名对应的字段值是否相同，相同则返回<br>
     * 如果给定字段值参数是{@code null} 且元素对象中的字段值也为{@code null}则认为相同
     *
     * @param <T>        集合元素类型
     * @param collection 集合，集合元素可以是Bean或者Map
     * @param fieldName  集合元素对象的字段名或map的键
     * @param fieldValue 集合元素对象的字段值或map的值
     * @return 满足条件的第一个元素
     * @since 3.1.0
     */
    public static <T> T findOneByField(Iterable<T> collection, final String fieldName, final Object fieldValue) {
        return findOne(collection, new Filter<T>() {
            @Override
            public boolean accept(T t) {
                if (t instanceof Map) {
                    final Map<?, ?> map = (Map<?, ?>) t;
                    final Object value = map.get(fieldName);
                    return ObjectUtil.equal(value, fieldValue);
                }

                // 普通Bean
                final Object value = ReflectUtil.getFieldValue(t, fieldName);
                return ObjectUtil.equal(value, fieldValue);
            }
        });
    }

    /**
     * 集合中匹配规则的数量
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @param matcher  匹配器，为空则全部匹配
     * @return 匹配数量
     */
    public static <T> int count(Iterable<T> iterable, Matcher<T> matcher) {
        int count = 0;
        if (null != iterable) {
            for (T t : iterable) {
                if (null == matcher || matcher.match(t)) {
                    count++;
                }
            }
        }
        return count;
    }

    // ---------------------------------------------------------------------- isEmpty

    /**
     * 集合是否为空
     *
     * @param collection 集合
     * @return 是否为空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Map是否为空
     *
     * @param map 集合
     * @return 是否为空
     * @see MapUtil#isEmpty(Map)
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return MapUtil.isEmpty(map);
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     * @see IterUtil#isEmpty(Iterable)
     */
    public static boolean isEmpty(Iterable<?> iterable) {
        return IterUtil.isEmpty(iterable);
    }

    /**
     * Iterator是否为空
     *
     * @param Iterator Iterator对象
     * @return 是否为空
     * @see IterUtil#isEmpty(Iterator)
     */
    public static boolean isEmpty(Iterator<?> Iterator) {
        return IterUtil.isEmpty(Iterator);
    }

    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isEmpty(Enumeration<?> enumeration) {
        return null == enumeration || false == enumeration.hasMoreElements();
    }

    // ---------------------------------------------------------------------- isNotEmpty

    /**
     * 集合是否为非空
     *
     * @param collection 集合
     * @return 是否为非空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return false == isEmpty(collection);
    }

    /**
     * Map是否为非空
     *
     * @param map 集合
     * @return 是否为非空
     * @see MapUtil#isNotEmpty(Map)
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return MapUtil.isNotEmpty(map);
    }

    /**
     * Iterable是否为空
     *
     * @param iterable Iterable对象
     * @return 是否为空
     * @see IterUtil#isNotEmpty(Iterable)
     */
    public static boolean isNotEmpty(Iterable<?> iterable) {
        return IterUtil.isNotEmpty(iterable);
    }

    /**
     * Iterator是否为空
     *
     * @param Iterator Iterator对象
     * @return 是否为空
     * @see IterUtil#isNotEmpty(Iterator)
     */
    public static boolean isNotEmpty(Iterator<?> Iterator) {
        return IterUtil.isNotEmpty(Iterator);
    }

    /**
     * Enumeration是否为空
     *
     * @param enumeration {@link Enumeration}
     * @return 是否为空
     */
    public static boolean isNotEmpty(Enumeration<?> enumeration) {
        return null != enumeration && enumeration.hasMoreElements();
    }

    /**
     * 是否包含{@code null}元素
     *
     * @param iterable 被检查的Iterable对象，如果为{@code null} 返回false
     * @return 是否包含{@code null}元素
     * @see IterUtil#hasNull(Iterable)
     * @since 3.0.7
     */
    public static boolean hasNull(Iterable<?> iterable) {
        return IterUtil.hasNull(iterable);
    }

    // ---------------------------------------------------------------------- zip

    /**
     * 映射键值（参考Python的zip()函数）<br>
     * 例如：<br>
     * keys = a,b,c,d<br>
     * values = 1,2,3,4<br>
     * delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}<br>
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param keys      键列表
     * @param values    值列表
     * @param delimiter 分隔符
     * @param isOrder   是否有序
     * @return Map
     * @since 3.0.4
     */
    public static Map<String, String> zip(String keys, String values, String delimiter, boolean isOrder) {
        return ArrayUtil.zip(StringUtil.split(keys, delimiter), StringUtil.split(values, delimiter), isOrder);
    }

    /**
     * 映射键值（参考Python的zip()函数），返回Map无序<br>
     * 例如：<br>
     * keys = a,b,c,d<br>
     * values = 1,2,3,4<br>
     * delimiter = , 则得到的Map是 {a=1, b=2, c=3, d=4}<br>
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param keys      键列表
     * @param values    值列表
     * @param delimiter 分隔符
     * @return Map
     */
    public static Map<String, String> zip(String keys, String values, String delimiter) {
        return zip(keys, values, delimiter, false);
    }

    /**
     * 映射键值（参考Python的zip()函数）<br>
     * 例如：<br>
     * keys = [a,b,c,d]<br>
     * values = [1,2,3,4]<br>
     * 则得到的Map是 {a=1, b=2, c=3, d=4}<br>
     * 如果两个数组长度不同，则只对应最短部分
     *
     * @param <K>    键类型
     * @param <V>    值类型
     * @param keys   键列表
     * @param values 值列表
     * @return Map
     */
    public static <K, V> Map<K, V> zip(Collection<K> keys, Collection<V> values) {
        if (isEmpty(keys) || isEmpty(values)) {
            return null;
        }

        final List<K> keyList = new ArrayList<K>(keys);
        final List<V> valueList = new ArrayList<V>(values);

        final int size = Math.min(keys.size(), values.size());
        final Map<K, V> map = new HashMap<K, V>((int) (size / 0.75));
        for (int i = 0; i < size; i++) {
            map.put(keyList.get(i), valueList.get(i));
        }

        return map;
    }

    /**
     * 将Entry集合转换为HashMap
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param entryIter entry集合
     * @return Map
     * @see IterUtil#toMap(Iterable)
     */
    public static <K, V> HashMap<K, V> toMap(Iterable<Entry<K, V>> entryIter) {
        return IterUtil.toMap(entryIter);
    }

    /**
     * 将数组转换为Map（HashMap），支持数组元素类型为：
     *
     * <pre>
     * Map.Entry
     * 长度大于1的数组（取前两个值），如果不满足跳过此元素
     * Iterable 长度也必须大于1（取前两个值），如果不满足跳过此元素
     * Iterator 长度也必须大于1（取前两个值），如果不满足跳过此元素
     * </pre>
     *
     * <pre>
     * Map&lt;Object, Object&gt; colorMap = CollectionUtil.toMap(new String[][] {{
     *     {"RED", "#FF0000"},
     *     {"GREEN", "#00FF00"},
     *     {"BLUE", "#0000FF"}});
     * </pre>
     * <p>
     * 参考：commons-lang
     *
     * @param array 数组。元素类型为Map.Entry、数组、Iterable、Iterator
     * @return {@link HashMap}
     * @see MapUtil#of(Object[])
     * @since 3.0.8
     */
    public static HashMap<Object, Object> toMap(Object[] array) {
        return MapUtil.of(array);
    }

    /**
     * 将集合转换为排序后的TreeSet
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> TreeSet<T> toTreeSet(Collection<T> collection, Comparator<T> comparator) {
        final TreeSet<T> treeSet = new TreeSet<T>(comparator);
        for (T t : collection) {
            treeSet.add(t);
        }
        return treeSet;
    }

    /**
     * {@link Iterator} 转为 {@link Iterable}
     *
     * @param <E>  元素类型
     * @param iter {@link Iterator}
     * @return {@link Iterable}
     * @see IterUtil#asIterable(Iterator)
     */
    public static <E> Iterable<E> asIterable(final Iterator<E> iter) {
        return IterUtil.asIterable(iter);
    }

    /**
     * {@link Iterable}转为{@link Collection}<br>
     * 首先尝试强转，强转失败则构建一个新的{@link ArrayList}
     *
     * @param <E>      集合元素类型
     * @param iterable {@link Iterable}
     * @return {@link Collection} 或者 {@link ArrayList}
     * @since 3.0.9
     */
    public static <E> Collection<E> toCollection(Iterable<E> iterable) {
        return (iterable instanceof Collection) ? (Collection<E>) iterable : newArrayList(iterable.iterator());
    }

    /**
     * 行转列，合并相同的键，值合并为列表<br>
     * 将Map列表中相同key的值组成列表做为Map的value<br>
     * 是{@link #toMapList(Map)}的逆方法<br>
     * 比如传入数据：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param mapList Map列表
     * @return Map
     * @see MapUtil#toListMap(Iterable)
     */
    public static <K, V> Map<K, List<V>> toListMap(Iterable<? extends Map<K, V>> mapList) {
        return MapUtil.toListMap(mapList);
    }

    /**
     * 列转行。将Map中值列表分别按照其位置与key组成新的map。<br>
     * 是{@link #toListMap(Iterable)}的逆方法<br>
     * 比如传入数据：
     *
     * <pre>
     * {
     *   a: [1,2,3,4]
     *   b: [1,2,3,]
     *   c: [1]
     * }
     * </pre>
     * <p>
     * 结果是：
     *
     * <pre>
     * [
     *  {a: 1, b: 1, c: 1}
     *  {a: 2, b: 2}
     *  {a: 3, b: 3}
     *  {a: 4}
     * ]
     * </pre>
     *
     * @param <K>     键类型
     * @param <V>     值类型
     * @param listMap 列表Map
     * @return Map列表
     * @see MapUtil#toMapList(Map)
     */
    public static <K, V> List<Map<K, V>> toMapList(Map<K, ? extends Iterable<V>> listMap) {
        return MapUtil.toMapList(listMap);
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterator   要加入的{@link Iterator}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Iterator<T> iterator) {
        if (null != collection && null != iterator) {
            while (iterator.hasNext()) {
                collection.add(iterator.next());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param iterable   要加入的内容{@link Iterable}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Iterable<T> iterable) {
        return addAll(collection, iterable.iterator());
    }

    /**
     * 加入全部
     *
     * @param <T>         集合元素类型
     * @param collection  被加入的集合 {@link Collection}
     * @param enumeration 要加入的内容{@link Enumeration}
     * @return 原集合
     */
    public static <T> Collection<T> addAll(Collection<T> collection, Enumeration<T> enumeration) {
        if (null != collection && null != enumeration) {
            while (enumeration.hasMoreElements()) {
                collection.add(enumeration.nextElement());
            }
        }
        return collection;
    }

    /**
     * 加入全部
     *
     * @param <T>        集合元素类型
     * @param collection 被加入的集合 {@link Collection}
     * @param values     要加入的内容数组
     * @return 原集合
     * @since 3.0.8
     */
    public static <T> Collection<T> addAll(Collection<T> collection, T[] values) {
        if (null != collection && null != values) {
            for (T value : values) {
                collection.add(value);
            }
        }
        return collection;
    }

    /**
     * 将另一个列表中的元素加入到列表中，如果列表中已经存在此元素则忽略之
     *
     * @param <T>       集合元素类型
     * @param list      列表
     * @param otherList 其它列表
     * @return 此列表
     */
    public static <T> List<T> addAllIfNotContains(List<T> list, List<T> otherList) {
        for (T t : otherList) {
            if (false == list.contains(t)) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 获取集合中指定下标的元素值，下标可以为负数，例如-1表示最后一个元素<br>
     * 如果元素越界，返回null
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param index      下标，支持负数
     * @return 元素值
     * @since 4.0.6
     */
    public static <T> T get(Collection<T> collection, int index) {
        final int size = collection.size();
        if (index < 0) {
            index += size;
        }

        //检查越界
        if (index >= size) {
            return null;
        }

        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            return list.get(index);
        } else {
            int i = 0;
            for (T t : collection) {
                if (i > index) {
                    break;
                } else if (i == index) {
                    return t;
                }
                i++;
            }
        }
        return null;
    }

    /**
     * 获取集合中指定多个下标的元素值，下标可以为负数，例如-1表示最后一个元素
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param indexes    下标，支持负数
     * @return 元素值列表
     * @since 4.0.6
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> getAny(Collection<T> collection, int... indexes) {
        final int size = collection.size();
        final ArrayList<T> result = new ArrayList<>();
        if (collection instanceof List) {
            final List<T> list = ((List<T>) collection);
            for (int index : indexes) {
                if (index < 0) {
                    index += size;
                }
                result.add(list.get(index));
            }
        } else {
            Object[] array = ((Collection<T>) collection).toArray();
            for (int index : indexes) {
                if (index < 0) {
                    index += size;
                }
                result.add((T) array[index]);
            }
        }
        return result;
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T>      集合元素类型
     * @param iterable {@link Iterable}
     * @return 第一个元素
     * @see IterUtil#getFirst(Iterable)
     * @since 3.0.1
     */
    public static <T> T getFirst(Iterable<T> iterable) {
        return IterUtil.getFirst(iterable);
    }

    /**
     * 获取集合的第一个元素
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @return 第一个元素
     * @see IterUtil#getFirst(Iterator)
     * @since 3.0.1
     */
    public static <T> T getFirst(Iterator<T> iterator) {
        return IterUtil.getFirst(iterator);
    }

    /**
     * 获取集合的最后一个元素
     *
     * @param <T>        集合元素类型
     * @param collection {@link Collection}
     * @return 最后一个元素
     * @since 4.1.10
     */
    public static <T> T getLast(Collection<T> collection) {
        return get(collection, -1);
    }

    /**
     * 从Map中获取指定键列表对应的值列表<br>
     * 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     * @since 3.0.8
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, K... keys) {
        final ArrayList<V> values = new ArrayList<>();
        for (K k : keys) {
            values.add(map.get(k));
        }
        return values;
    }

    /**
     * 从Map中获取指定键列表对应的值列表<br>
     * 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     * @since 3.0.9
     */
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, Iterable<K> keys) {
        return valuesOfKeys(map, keys.iterator());
    }

    /**
     * 从Map中获取指定键列表对应的值列表<br>
     * 如果key在map中不存在或key对应值为null，则返回值列表对应位置的值也为null
     *
     * @param <K>  键类型
     * @param <V>  值类型
     * @param map  {@link Map}
     * @param keys 键列表
     * @return 值列表
     * @since 3.0.9
     */
    public static <K, V> ArrayList<V> valuesOfKeys(Map<K, V> map, Iterator<K> keys) {
        final ArrayList<V> values = new ArrayList<>();
        while (keys.hasNext()) {
            values.add(map.get(keys.next()));
        }
        return values;
    }

    // ------------------------------------------------------------------------------------------------- sort

    /**
     * 排序集合，排序不会修改原集合
     *
     * @param <T>        集合元素类型
     * @param collection 集合
     * @param comparator 比较器
     * @return treeSet
     */
    public static <T> List<T> sort(Collection<T> collection, Comparator<? super T> comparator) {
        List<T> list = new ArrayList<T>(collection);
        Collections.sort(list, comparator);
        return list;
    }

    /**
     * 针对List排序，排序会修改原List
     *
     * @param <T>  元素类型
     * @param list 被排序的List
     * @param c    {@link Comparator}
     * @return 原list
     * @see Collections#sort(List, Comparator)
     */
    public static <T> List<T> sort(List<T> list, Comparator<? super T> c) {
        Collections.sort(list, c);
        return list;
    }

    /**
     * 根据汉字的拼音顺序排序
     *
     * @param collection 集合，会被转换为List
     * @return 排序后的List
     * @since 4.0.8
     */
    public static <T> List<String> sortByPinyin(Collection<String> collection) {
        return sort(collection, new PinyinComparator());
    }

    /**
     * 根据汉字的拼音顺序排序
     *
     * @param list List
     * @return 排序后的List
     * @since 4.0.8
     */
    public static <T> List<String> sortByPinyin(List<String> list) {
        return sort(list, new PinyinComparator());
    }

    /**
     * 排序Map
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        Map
     * @param comparator Entry比较器
     * @return {@link TreeMap}
     * @since 3.0.9
     */
    public static <K, V> TreeMap<K, V> sort(Map<K, V> map, Comparator<? super K> comparator) {
        final TreeMap<K, V> result = new TreeMap<K, V>(comparator);
        result.putAll(map);
        return result;
    }

    /**
     * 通过Entry排序，可以按照键排序，也可以按照值排序，亦或者两者综合排序
     *
     * @param <K>             键类型
     * @param <V>             值类型
     * @param entryCollection Entry集合
     * @param comparator      {@link Comparator}
     * @return {@link LinkedList}
     * @since 3.0.9
     */
    public static <K, V> LinkedHashMap<K, V> sortToMap(Collection<Entry<K, V>> entryCollection, Comparator<Entry<K, V>> comparator) {
        List<Entry<K, V>> list = new LinkedList<>(entryCollection);
        Collections.sort(list, comparator);

        LinkedHashMap<K, V> result = new LinkedHashMap<>();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * 通过Entry排序，可以按照键排序，也可以按照值排序，亦或者两者综合排序
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param map        被排序的Map
     * @param comparator {@link Comparator}
     * @return {@link LinkedList}
     * @since 3.0.9
     */
    public static <K, V> LinkedHashMap<K, V> sortByEntry(Map<K, V> map, Comparator<Entry<K, V>> comparator) {
        return sortToMap(map.entrySet(), comparator);
    }

    /**
     * 将Set排序（根据Entry的值）
     *
     * @param <K>        键类型
     * @param <V>        值类型
     * @param collection 被排序的{@link Collection}
     * @return 排序后的Set
     */
    public static <K, V> List<Entry<K, V>> sortEntryToList(Collection<Entry<K, V>> collection) {
        List<Entry<K, V>> list = new LinkedList<>(collection);
        Collections.sort(list, new Comparator<Entry<K, V>>() {

            @SuppressWarnings({"rawtypes", "unchecked"})
            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                V v1 = o1.getValue();
                V v2 = o2.getValue();

                if (v1 instanceof Comparable) {
                    return ((Comparable) v1).compareTo(v2);
                } else {
                    return v1.toString().compareTo(v2.toString());
                }
            }
        });
        return list;
    }

    // ------------------------------------------------------------------------------------------------- forEach

    /**
     * 循环遍历 {@link Iterator}，使用{@link Consumer} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <T>      集合元素类型
     * @param iterator {@link Iterator}
     * @param consumer {@link Consumer} 遍历的每条数据处理器
     */
    public static <T> void forEach(Iterator<T> iterator, Consumer<T> consumer) {
        int index = 0;
        while (iterator.hasNext()) {
            consumer.accept(iterator.next(), index);
            index++;
        }
    }

    /**
     * 循环遍历 {@link Enumeration}，使用{@link Consumer} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <T>         集合元素类型
     * @param enumeration {@link Enumeration}
     * @param consumer    {@link Consumer} 遍历的每条数据处理器
     */
    public static <T> void forEach(Enumeration<T> enumeration, Consumer<T> consumer) {
        int index = 0;
        while (enumeration.hasMoreElements()) {
            consumer.accept(enumeration.nextElement(), index);
            index++;
        }
    }

    /**
     * 循环遍历Map，使用{@link KVConsumer} 接受遍历的每条数据，并针对每条数据做处理
     *
     * @param <K>        Key类型
     * @param <V>        Value类型
     * @param map        {@link Map}
     * @param kvConsumer {@link KVConsumer} 遍历的每条数据处理器
     */
    public static <K, V> void forEach(Map<K, V> map, KVConsumer<K, V> kvConsumer) {
        int index = 0;
        for (Entry<K, V> entry : map.entrySet()) {
            kvConsumer.accept(entry.getKey(), entry.getValue(), index);
            index++;
        }
    }

    /**
     * 分组，按照{@link Hash}接口定义的hash算法，集合中的元素放入hash值对应的子列表中
     *
     * @param <T>        元素类型
     * @param collection 被分组的集合
     * @param hash       Hash值算法，决定元素放在第几个分组的规则
     * @return 分组后的集合
     */
    public static <T> List<List<T>> group(Collection<T> collection, Hash<T> hash) {
        final List<List<T>> result = new ArrayList<>();
        if (isEmpty(collection)) {
            return result;
        }
        if (null == hash) {
            // 默认hash算法，按照元素的hashCode分组
            hash = new Hash<T>() {
                @Override
                public int hash(T t) {
                    return null == t ? 0 : t.hashCode();
                }
            };
        }

        int index;
        List<T> subList;
        for (T t : collection) {
            index = hash.hash(t);
            if (result.size() - 1 < index) {
                while (result.size() - 1 < index) {
                    result.add(null);
                }
                result.set(index, newArrayList(t));
            } else {
                subList = result.get(index);
                if (null == subList) {
                    result.set(index, newArrayList(t));
                } else {
                    subList.add(t);
                }
            }
        }
        return result;
    }

    /**
     * 根据元素的指定字段名分组，非Bean都放在第一个分组中
     *
     * @param <T>        元素类型
     * @param collection 集合
     * @param fieldName  元素Bean中的字段名，非Bean都放在第一个分组中
     * @return 分组列表
     */
    public static <T> List<List<T>> groupByField(Collection<T> collection, final String fieldName) {
        return group(collection, new Hash<T>() {
            private List<Object> fieldNameList = new ArrayList<>();

            @Override
            public int hash(T t) {
                if (null == t || false == BeanUtil.isBean(t.getClass())) {
                    // 非Bean放在同一子分组中
                    return 0;
                }
                final Object value = ReflectUtil.getFieldValue(t, fieldName);
                int hash = fieldNameList.indexOf(value);
                if (hash < 0) {
                    fieldNameList.add(value);
                    return fieldNameList.size() - 1;
                } else {
                    return hash;
                }
            }
        });
    }

    /**
     * 反序给定List，会在原List基础上直接修改
     *
     * @param <T>  元素类型
     * @param list 被反转的List
     * @return 反转后的List
     * @since 4.0.6
     */
    public static <T> List<T> reverse(List<T> list) {
        Collections.reverse(list);
        return list;
    }

    /**
     * 设置或增加元素。当index小于List的长度时，替换指定位置的值，否则在尾部追加
     *
     * @param list    List列表
     * @param index   位置
     * @param element 新元素
     * @return 原List
     * @since 4.1.2
     */
    public static <T> List<T> setOrAppend(List<T> list, int index, T element) {
        if (index < list.size()) {
            list.set(index, element);
        } else {
            list.add(element);
        }
        return list;
    }

    // ---------------------------------------------------------------------------------------------- Interface start

    /**
     * 针对一个参数做相应的操作
     *
     * @param <T> 处理参数类型
     * @author Looly
     */
    public static interface Consumer<T> {
        /**
         * 接受并处理一个参数
         *
         * @param value 参数值
         * @param index 参数在集合中的索引
         */
        void accept(T value, int index);
    }

    /**
     * 针对两个参数做相应的操作，例如Map中的KEY和VALUE
     *
     * @param <K> KEY类型
     * @param <V> VALUE类型
     * @author Looly
     */
    public static interface KVConsumer<K, V> {
        /**
         * 接受并处理一对参数
         *
         * @param key   键
         * @param value 值
         * @param index 参数在集合中的索引
         */
        void accept(K key, V value, int index);
    }

    /**
     * Hash计算接口
     *
     * @param <T> 被计算hash的对象类型
     * @author looly
     * @since 3.2.2
     */
    public static interface Hash<T> {
        /**
         * 计算Hash值
         *
         * @param t 对象
         * @return hash
         */
        int hash(T t);
    }
    // ---------------------------------------------------------------------------------------------- Interface end
}
