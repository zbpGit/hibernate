/**
* ${table.comment!}功能接口API
*/
import request from '@/utils/request'
import qs from 'qs'

const get${entity} = query => request({
  url: '/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen?replace("-","/")}<#else>${table.entityPath}</#if>',
  method: 'get',
  params: query
})

const get${entity}ById = id => request({
  url: '/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen?replace("-","/")}<#else>${table.entityPath}</#if>/' + id,
  method: 'get'
})

const save${entity} = data => request({
  url: '/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen?replace("-","/")}<#else>${table.entityPath}</#if>/save',
  method: 'post',
  data
})

const delete${entity} = data => {
  return request({
    url: '/<#if controllerMappingHyphenStyle??>${controllerMappingHyphen?replace("-","/")}<#else>${table.entityPath}</#if>/delete',
    method: 'post',
    headers: { 'content-type': 'application/x-www-form-urlencoded' },
    data: qs.stringify(data, { indices: false })
  })
}

export default {
  get${entity},
  get${entity}ById,
  save${entity},
  delete${entity}
}
