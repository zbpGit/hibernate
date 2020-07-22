<template>
  <el-dialog :visible.sync="dialogVisible" title="${table.comment!}">
    <el-form ref="showForm" label-width="110px" label-suffix=":">
    <#-- ----------  BEGIN 字段循环遍历  ---------->
    <#list table.fields as field>
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form-item label="<#if field.comment!?length gt 0>${field.comment}<#else>${field.propertyName}</#if>">
            <span>{{ ${firstCharToLowerEntity}.${field.propertyName} }}</span>
          </el-form-item>
        </el-col>
      </el-row>
    </#list>
    <#------------  END 字段循环遍历  ---------->
    </el-form>
    <div slot="footer" class="dialog-footer">
      <el-button type="primary" @click="dialogVisible = false">返回</el-button>
    </div>
  </el-dialog>
</template>

<script>
import ${firstCharToLowerEntity}Api from '@/api/${cfg.moduleName}/${firstCharToLowerEntity}'

export default {
  name: 'show-${entity?lower_case}',
  props: {
    showDialogVisible: {
      required: true,
      type: Boolean,
      default: false
    },
    ${firstCharToLowerEntity}Id: {
      required: true,
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
      }
    }
  },
  computed: {
    dialogVisible: {
      get() {
        return this.showDialogVisible
      },
      set(val) {
        this.$emit('update:showDialogVisible', val)
      }
    }
  },
  methods: {
    get${entity}() {
      ${firstCharToLowerEntity}Api.get${entity}ById(this.${firstCharToLowerEntity}Id).then(response => {
        this.${firstCharToLowerEntity} = response
      })
    }
  },
  created() {
    this.get${entity}()
  }
}
</script>

<style scoped>

</style>
