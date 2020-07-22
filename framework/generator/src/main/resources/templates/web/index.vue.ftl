<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-search" @click="handleFilter">查询</el-button>
      <el-button v-waves class="filter-item" type="primary" icon="el-icon-plus" style="margin-left: 5px;" @click="edit${entity}()">新增</el-button>
      <el-button v-waves class="filter-item" type="danger" icon="el-icon-delete" style="margin-left: 5px;" @click="multipleDelete">批量删除</el-button>
    </div>

    <el-table v-loading="listLoading" :data="listData" @selection-change="handleSelectionChange" @row-dblclick="show${entity}" border style="width: 100%;">
      <el-table-column align="center" type="index" width="50" />
      <el-table-column align="center" type="selection" width="50" />
  <#-- ----------  BEGIN 字段循环遍历  ---------->
  <#list table.fields as field>
    <#if field.keyFlag>
      <#-- 默认主键字段上面加点击链接 -->
      <el-table-column align="center" label="<#if field.comment!?length gt 0>${field.comment}<#else>${field.propertyName}</#if>">
        <template slot-scope="scope">
          <span class="link-type" @click="show${entity}(scope.row)">{{ scope.row.${field.propertyName} }}</span>
        </template>
      </el-table-column>
    <#else>
      <#-- 普通字段 -->
      <el-table-column align="center" prop="${field.propertyName}"<#if field.propertyType=="Date"> :formatter="formatDate"</#if> label="<#if field.comment!?length gt 0>${field.comment}<#else>${field.propertyName}</#if>" show-overflow-tooltip />
    </#if>
  </#list>
  <#------------  END 字段循环遍历  ---------->
      <el-table-column align="center" fixed="right" width="120" label="操作">
        <template slot-scope="scope">
          <el-button-group>
            <el-button type="primary" size="mini" icon="el-icon-edit" @click="edit${entity}(scope.row)"></el-button>
            <el-button type="danger" size="mini" icon="el-icon-delete" @click="delete${entity}(scope.row)"></el-button>
          </el-button-group>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="listQuery.pageNum" :limit.sync="listQuery.pageSize" @pagination="getListData" />

    <!--查看对话框-->
    <show-dialog v-if="showDialogVisible" ref="showDialog" :${firstCharToLowerEntity}Id="${firstCharToLowerEntity}Id" :showDialogVisible.sync="showDialogVisible" />

    <!--编辑对话框-->
    <edit-dialog v-if="editDialogVisible" ref="editDialog" :${firstCharToLowerEntity}Id="${firstCharToLowerEntity}Id" :editDialogVisible.sync="editDialogVisible" @getListData="getListData" />

  </div>
</template>

<script>
import ${firstCharToLowerEntity}Api from '@/api/${cfg.moduleName}/${firstCharToLowerEntity}'
import waves from '@/directive/waves' // Waves directive
import Pagination from '@/components/Pagination' // Secondary package based on el-pagination
import showDialog from '@/views/${cfg.moduleName}/${entity?lower_case}/show'
import editDialog from '@/views/${cfg.moduleName}/${entity?lower_case}/edit'

export default {
  components: { Pagination, showDialog, editDialog },
  directives: { waves },
  data() {
    return {
      listData: [],
      total: 0,
      listLoading: false,
      showDialogVisible: false,
      editDialogVisible: false,
      ${firstCharToLowerEntity}Id: undefined,
      listQuery: {
        pageNum: 1,
        pageSize: 20
      },
      multipleSelection: []
    }
  },
  created() {
    this.getListData()
  },
  methods: {
    formatDate(row, column) {
      let date = row[column.property]
      date = date.substring(0, 19)
      date = date.replace('T', ' ')
      return date
    },
    handleFilter() {
      this.listQuery.pageNum = 1
      this.getListData()
    },
    handleSelectionChange(val) {
      this.multipleSelection = []
      val.forEach(item => {
        this.multipleSelection.push(item.id)
      })
    },
    getListData() {
      this.listLoading = true
      ${firstCharToLowerEntity}Api.get${entity}(this.listQuery).then(response => {
        this.listData = response.list
        this.total = response.total
        this.listLoading = false
      }).catch(() => {
        this.listLoading = false
      })
    },
    show${entity}(row) {
      this.${firstCharToLowerEntity}Id = row.id
      this.showDialogVisible = true
    },
    edit${entity}(row) {
      if (row) {
        this.${firstCharToLowerEntity}Id = row.id
      } else {
        this.${firstCharToLowerEntity}Id = undefined
      }
      this.editDialogVisible = true
    },
    delete${entity}(row) {
      this.$confirm('确定删除该条数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        const data = { ids: [row.id] }
        ${firstCharToLowerEntity}Api.delete${entity}(data).then(() => {
          this.getListData()
          this.$message.success('删除成功!')
        })
      })
    },
    multipleDelete() {
      if (this.multipleSelection.length !== 0) {
        this.$confirm('确定删除选中的数据吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          const data = { ids: this.multipleSelection }
          ${firstCharToLowerEntity}Api.delete${entity}(data).then(() => {
            this.getListData()
            this.$message.success('删除成功!')
          })
        }).catch(_ => {})
      } else {
        this.$message.info('请选择要删除的数据')
      }
    }
  }
}
</script>
