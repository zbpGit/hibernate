<template>
  <el-dialog :visible.sync="dialogVisible" title="${table.comment!}">
    <el-form ref="editForm" :rules="rules" :model="${firstCharToLowerEntity}" label-width="110px" label-suffix=":">
    <#-- ----------  BEGIN 字段循环遍历  ---------->
    <#list table.fields as field>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="<#if field.comment!?length gt 0>${field.comment}<#else>${field.propertyName}</#if>" prop="${field.propertyName}">
            <el-input v-model="${firstCharToLowerEntity}.${field.propertyName}" />
          </el-form-item>
        </el-col>
      </el-row>
    </#list>
    <#------------  END 字段循环遍历  ---------->
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save${entity}">保存</el-button>
    </div>
  </el-dialog>
</template>

<script>
import ${firstCharToLowerEntity}Api from '@/api/${cfg.moduleName}/${firstCharToLowerEntity}'

export default {
  name: 'edit-${entity?lower_case}',
  props: {
    editDialogVisible: {
      required: true,
      type: Boolean,
      default: false
    },
    ${firstCharToLowerEntity}Id: {
      type: String,
      default: undefined
    }
  },
  data() {
    return {
      ${firstCharToLowerEntity}: {
      <#-- ----------  BEGIN 字段循环遍历  ---------->
      <#list table.fields as field>
        ${field.propertyName}: undefined<#if field_has_next>,</#if>
      </#list>
      <#------------  END 字段循环遍历  ---------->
      },
      rules: {
        code: [{ required: true, message: '编号不能为空', trigger: 'blur' }],
        name: [{ required: true, message: '名称不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.editDialogVisible
      },
      set(val) {
        this.$emit('update:editDialogVisible', val)
      }
    }
  },
  methods: {
    get${entity}() {
      ${firstCharToLowerEntity}Api.get${entity}ById(this.${firstCharToLowerEntity}Id).then(response => {
        this.${firstCharToLowerEntity} = response
      })
    },
    save${entity}() {
      this.$refs['editForm'].validate((valid) => {
        if (valid) {
          ${firstCharToLowerEntity}Api.save${entity}(this.${firstCharToLowerEntity}).then(() => {
            this.$emit('getListData')
            this.dialogVisible = false
            this.$message.success('保存成功!')
          })
        }
      })
    }
  },
  created() {
    if (this.${firstCharToLowerEntity}Id) {
      this.get${entity}()
    }
  }
}
</script>

<style scoped>

</style>
