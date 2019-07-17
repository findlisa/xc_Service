package com.xuecheng.api.cms;

import com.xuecheng.framework.domain.cms.CmsPage;
import com.xuecheng.framework.domain.cms.request.QueryPageRequest;
import com.xuecheng.framework.domain.cms.response.CmsPageResult;
import com.xuecheng.framework.model.response.QueryResponseResult;
import com.xuecheng.framework.model.response.ResponseResult;
import io.swagger.annotations.ApiOperation;

public interface CmsPageControllerApi {
    //页面查询
    @ApiOperation("页面查询")
    QueryResponseResult findList(int page, int size, QueryPageRequest queryPageRequest);

    @ApiOperation("页面添加")
    CmsPageResult save(CmsPage cmsPage);

    @ApiOperation("修改页面信息")
    CmsPageResult edit(String id,CmsPage cmsPage);

    @ApiOperation("根据id查询信息")
    CmsPage findById(String id);

    @ApiOperation("删除信息")
    ResponseResult deleteById(String id);
}
