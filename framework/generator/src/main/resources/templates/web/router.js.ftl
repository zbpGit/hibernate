{
  path: '<#if controllerMappingHyphenStyle??>${controllerMappingHyphen}<#else>${table.entityPath}</#if>',
  name: '${entity}',
  component: () => import('@/views/${cfg.moduleName}/${entity?lower_case}/index'),
  meta: { title: '${table.comment!}', icon: 'list' }
}