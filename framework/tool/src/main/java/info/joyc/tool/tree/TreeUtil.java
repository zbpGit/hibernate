package info.joyc.tool.tree;

import info.joyc.tool.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * info.joyc.util.tree.TreeUtil.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : List转Tree操作类
 * @since : 2018-01-02 17:27
 */
public class TreeUtil {

    private static final Logger log = LoggerFactory.getLogger(TreeUtil.class);
    
    /**
     * 对List中的元素根据父节点Id进行分类
     *
     * @param u
     * @return
     */
    public static Map<String, List<TreeNode>> groupListByParentId(List<? extends TreeNode> u) {
        // 定义一个map集合用于分组
        Map<String, List<TreeNode>> mapList = new HashMap<String, List<TreeNode>>();
        // 主要是把父亲节点相同的每个数据进行分组。然后存放到以父亲节点为Key，List<T>为value的Map中
        for (int i = 0; i < u.size(); i++) {
            // 将循环读取的结果放入对象中
            TreeNode treeNode = u.get(i);
            // 如果在这个map中包含有相同的键，这创建一个集合将其存起来
            if (mapList.containsKey(treeNode.getAutoParentId())) {
                List<TreeNode> syn = mapList.get(treeNode.getAutoParentId());
                syn.add(treeNode);
                mapList.put(treeNode.getAutoParentId(), syn);
                // 如果没有包含相同的键，在创建一个集合保存数据
            } else {
                List<TreeNode> treeNodes = new ArrayList<TreeNode>();
                treeNodes.add(treeNode);
                mapList.put(treeNode.getAutoParentId(), treeNodes);
            }
        }

        return mapList;
    }

    /**
     * 智能自动创建标志
     */
    private final static String AUTO_CREATE = "AUTO";

    /**
     * 根据List智能创建List树
     *
     * @param u
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static List<TreeNode> buildListTree(List<? extends TreeNode> u, Class<?> clazz) {
        return buildListTree(u, AUTO_CREATE, clazz);
    }

    /**
     * 根据List创建List树
     *
     * @param u
     * @param rootId
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static List<TreeNode> buildListTree(List<? extends TreeNode> u, String rootId, Class<?> clazz) {
        if (StringUtil.equals(rootId, AUTO_CREATE)) {
            List<String> autoIds = new ArrayList<>();
            // 如果集合中有父节点号为null的，则以null为rootId
            boolean nullIsRoot = false;
            if (u != null) {
                // 循环第一次，加载所有父节点
                for (TreeNode treeNode : u) {
                    String autoParentId = treeNode.getAutoParentId();
                    if (autoParentId != null) {
                        autoIds.add(treeNode.getAutoId());
                    } else {
                        nullIsRoot = true;
                        break;
                    }
                }
                if (nullIsRoot) {
                    rootId = null;
                } else {
                    // 循环第二次，匹配的ParentId是否存在autoIds，第一个不存在为rootId
                    for (TreeNode treeNode : u) {
                        if (!autoIds.contains(treeNode.getAutoParentId())) {
                            rootId = treeNode.getAutoParentId();
                            break;
                        }
                    }
                }
            }
        }
        return buildTree(groupListByParentId(u), rootId, clazz);
    }


    /**
     * 根据已经分组好的List和rootId来创建List树
     *
     * @param groupListByParentId
     * @param rootId
     */

    private static boolean mark = false;

    private static List<TreeNode> buildTree(Map<String, List<TreeNode>> mapList, String rootId, Class<?> clazz) {

        // 遍历map集合，取出根节点,这里的rootId,是指最顶层节点的父节点,不管最顶层的节点是一个还是多个
        List<TreeNode> rootList = new ArrayList<TreeNode>();
        for (Map.Entry<String, List<TreeNode>> tempMap : mapList.entrySet()) {
            rootList = tempMap.getValue();
            if ((rootId == null && rootList.get(0).getAutoParentId() == null) || (rootList.get(0).getAutoParentId().equals(rootId))) {
                mapList.remove(rootId);
                break;
            }
        }
        Integer initSize = mapList.size();
        // 遍历Map，由于遍历一次不一定可以全部找到字树父亲节点，所有有了上面的While循环，和KeyList.
        while (mapList.size() != 0) {
            Integer size = mapList.size();
            Iterator<Map.Entry<String, List<TreeNode>>> iteratorRoot = mapList.entrySet().iterator();
            while (iteratorRoot.hasNext()) {
                Map.Entry<String, List<TreeNode>> firtMap = iteratorRoot.next();
                // secList，一个等待挂载到父亲节点下的子树
                List<TreeNode> secList = firtMap.getValue();
                // 深度优先遍历
                for (int i = 0; i < rootList.size(); i++) {
                    recursiveTree(rootList, secList);
                    if (mark) {
                        mark = false;
                        iteratorRoot.remove();
                        break;
                    }
                }
            }
            // 这个判断，主要用于判断根节点的父节点Id是否输入正确。如果不正确，在遍历一遍后，mapList的长度还是会和最初的长度一样
            if (initSize.equals(mapList.size())) {
                throw new RuntimeException("根节点[" + rootId + "]的父节点输入错误");
            }
            // 讲道理，在上面这些代码执行完之后，如果mapList.size()还是和之前一样，就证明已经迭代完毕，剩下的，都是一些在root找不到父节点的子树，需要定义一个虚拟的root挂载
            if (size.equals(mapList.size())) {
                // 首先，创建一个节点
                TreeNode newNode = null;
                try {
                    newNode = (TreeNode) clazz.newInstance();
                } catch (ReflectiveOperationException e) {
                	log.error(e.getMessage(),e);
                    throw new RuntimeException("传入的class类未能强转为TreeNode类");
                }
                List<TreeNode> virtualList = new ArrayList<>();
                List<String> autoIds = new ArrayList<>();
                for (List<TreeNode> treeNodeList : mapList.values()) {
                    autoIds = treeNodeList.stream().map(TreeNode::getAutoId).collect(Collectors.toList());
                }
                Iterator<Map.Entry<String, List<TreeNode>>> iteratorVirtual = mapList.entrySet().iterator();
                // 第一次循环，找寻到虚拟树的根节点
                while (iteratorVirtual.hasNext()) {
                    Map.Entry<String, List<TreeNode>> firtMap = iteratorVirtual.next();
                    if (!autoIds.contains(firtMap.getKey())) {
                        List<TreeNode> secList = firtMap.getValue();
                        virtualList.addAll(secList);
                        iteratorVirtual.remove();
                    }
                }
                if (virtualList.size() > 0) {
                    // 从集合里重新获得迭代器
                    iteratorVirtual = mapList.entrySet().iterator();
                    while (iteratorVirtual.hasNext()) {
                        Map.Entry<String, List<TreeNode>> firtMap = iteratorVirtual.next();
                        // secList，一个等待挂载到父亲节点下的子树
                        List<TreeNode> secList = firtMap.getValue();
                        // 深度优先遍历
                        for (int i = 0; i < virtualList.size(); i++) {
                            recursiveTree(virtualList, secList);
                            if (mark) {
                                mark = false;
                                iteratorVirtual.remove();
                                break;
                            }
                        }
                    }
                } else {
                    throw new RuntimeException("虚拟树的根节点找寻失败");
                }
                // 设置这个节点的名称，并把这个子树挂载到这个节点下
                //newNode.setTreeNodeName("无区域-节点" + secList.get(0).getAutoParentId());
                newNode.setChildren(virtualList);
                rootList.add(newNode);
                break;
            }
        }
        return rootList;
    }

    /**
     * 传递进来一个子树，一个待挂载的子树
     * 递归子树，直到挂载成功，或者，递归完毕。
     *
     * @param list
     * @param chirldrenTree
     * @return
     */
    public static List<TreeNode> recursiveTree(List<TreeNode> list, List<TreeNode> chirldrenTree) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getAutoId() != null) {
                if (list.get(i).getAutoId().equals(chirldrenTree.get(0).getAutoParentId())) {
                    list.get(i).setChildren(chirldrenTree);
                    mark = true;
                    return list;
                } else if (list.get(i).getChildren() != null) {
                    list.get(i).setChildren(
                            recursiveTree(list.get(i).getChildren(),
                                    chirldrenTree));
                }
            }
        }
        return list;
    }
}
