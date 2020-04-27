package org.ecs.schedule.controller;

import org.ecs.schedule.web.util.JqueryDataTable;
import org.ecs.schedule.web.util.JsonResult;
import org.ecs.util.dao.PageDataList;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.ArrayList;

@Controller
public class BaseController {

    @InitBinder
    public void initBinder(ServletRequestDataBinder binder) {

    }

    protected <T> JsonResult<T> success() {
        return this.success("操作成功", null);
    }

    protected <T> JsonResult<T> success(T data) {
        return this.success("操作成功", data);
    }

    protected <T> JsonResult<T> success(String msg, T data) {
        JsonResult<T> result = new JsonResult<T>(JsonResult.Status.SUCCESS, msg, data);
        return result;
    }

    protected <T> JsonResult<T> error() {
        return this.error("系统错误");
    }


    protected <T> JsonResult<T> error(String msg) {
        JsonResult<T> result = new JsonResult<T>(JsonResult.Status.ERROR, msg);
        return result;
    }

    public String redict(String page) {

        return "redirect:" + page;
    }

    public <T> JqueryDataTable<T> dataTable(PageDataList<T> page) {
        JqueryDataTable<T> t = new JqueryDataTable<>();
        if (null != page) {
            t.setData(page.getRows());
            t.setRecordsFiltered(page.getTotal());
            t.setRecordsTotal(page.getTotal());
        } else {
            t.setData(new ArrayList<T>());
            t.setRecordsFiltered(0);
            t.setRecordsTotal(0);
        }
        return t;
    }

}
