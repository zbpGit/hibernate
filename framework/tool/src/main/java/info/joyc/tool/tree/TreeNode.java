package info.joyc.tool.tree;

import java.util.List;

/**
 * info.joyc.util.tree.TreeNode.java
 * ==============================================
 * Copy right 2015-2018 by http://www.joyc.info
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : qiuzh
 * @version : v1.0.0
 * @desc : List转Tree实体接口类
 * @since : 2018-01-02 17:25
 */
public interface TreeNode {

    /**
     * 自己的节点号
     *
     * @return
     */
    String getAutoId();

    /**
     * 获取父节点号
     */
    String getAutoParentId();

    /**
     * 获取EasyUI的显示名称
     */
    String getText();

    /**
     * 获取孩子树
     *
     * @return
     */
    List<TreeNode> getChildren();

    /**
     * 设置孩子树
     *
     * @param list
     */
    void setChildren(List<TreeNode> list);
}
