package com.hibernate.web.system;

import com.github.pagehelper.PageInfo;
import com.hibernate.entity.system.CharacterUser;
import com.hibernate.service.system.CharacterUserService;
import info.joyc.core.enums.DataStatusEnum;
import info.joyc.tool.lang.Assert;
import info.joyc.tool.util.StringUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.hibernate.web.system.CharacterUserController.java
 * ==============================================
 * Copy right 2015-2020 by http://www.com
 * ----------------------------------------------
 * This is not a free software, without any authorization is not allowed to use and spread.
 * ==============================================
 *
 * @author : zhangbeiping
 * @version : v1.0.0
 * @desc : 系统-用户角色关联表控制类
 * @since : 2020-07-23 17:31
 */
@RestController
@Api(value = "系统-用户角色关联表Controller", description = "系统-用户角色关联表相关api", tags = {"系统-用户角色关联表操作接口"})
@RequestMapping("/character-user")
public class CharacterUserController {

    @Autowired
    private CharacterUserService characterUserService;

    @ApiOperation(value = "获取系统-用户角色关联表列表", notes = "获取系统-用户角色关联表列表")
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "query", name = "statusIds", value = "数据状态集，逗号隔开", required = false, dataType = "String"),
            @ApiImplicitParam(paramType = "query", name = "pageNum", value = "页码", required = false, dataType = "int"),
            @ApiImplicitParam(paramType = "query", name = "pageSize", value = "每页显示数据行数", required = false, dataType = "int")
    })
    @GetMapping(value = "")
    public ResponseEntity<PageInfo<CharacterUser>> getCharacterUserList(@RequestParam(required = false) String statusIds,
                                                                        @RequestParam(required = false, defaultValue = "0") Integer pageNum,
                                                                        @RequestParam(required = false, defaultValue = "0") Integer pageSize) {
        Map<String, Object> parameterMap = new HashMap<>();
        if (StringUtil.isNotBlank(statusIds)) {
            parameterMap.put("statusIds", Arrays.asList(statusIds.split(",")));
        } else {
            parameterMap.put("status", DataStatusEnum.Enabled.getIndex());
        }
        parameterMap.put("pageNum", pageNum);
        parameterMap.put("pageSize", pageSize);
        return ResponseEntity.ok(new PageInfo<>(characterUserService.getCharacterUserList(parameterMap)));
    }

    @ApiOperation(value = "获取单个系统-用户角色关联表详细信息", notes = "根据url的id来获取系统-用户角色关联表详细信息")
    @ApiImplicitParam(paramType = "path", name = "id", value = "主键ID", required = true, dataType = "string")
    @GetMapping(value = "/{id}")
    public ResponseEntity<CharacterUser> getCharacterUser(@PathVariable String id) {
        CharacterUser characterUser = new CharacterUser();
        characterUser.setId(id);
        return ResponseEntity.ok(characterUserService.find(characterUser));
    }

    @ApiOperation(value = "保存单个系统-用户角色关联表", notes = "根据CharacterUser对象保存系统-用户角色关联表")
    @ApiImplicitParam(paramType = "body", name = "characterUser", value = "系统-用户角色关联表详细实体CharacterUser", required = true, dataType = "CharacterUser")
    @PostMapping(value = "/save")
    public ResponseEntity<CharacterUser> saveCharacterUser(@RequestBody CharacterUser characterUser) {
        Assert.notNull(characterUser, "保存的对象不能为空!");
        Assert.notNull(characterUser.getStatus(), "保存的对象数据状态不能为空!");
        return new ResponseEntity<>(characterUserService.save(characterUser), HttpStatus.CREATED);
    }

    @ApiOperation(value = "批量删除系统-用户角色关联表", notes = "根据url的ids来指定删除对象")
    @ApiImplicitParam(paramType = "form", name = "ids", value = "系统-用户角色关联表IDs", required = true, allowMultiple = true, dataType = "string")
    @PostMapping(value = "/delete")
    public ResponseEntity<Integer> deleteCharacterUser(@RequestParam List<String> ids) {
        int deleteCount = characterUserService.deleteBatchByIds(ids);
        Assert.state(deleteCount != 0, "无此ID对应的数据对象！");
        return ResponseEntity.ok(deleteCount);
    }
}
