package org.ecs.schedule.controller.jobclient;

import org.apache.commons.collections.CollectionUtils;
import org.ecs.schedule.controller.BaseController;
import org.ecs.schedule.domain.net.CuckooNetClientInfo;
import org.ecs.schedule.domain.net.CuckooNetRegistJob;
import org.ecs.schedule.domain.net.CuckooNetServerInfo;
import org.ecs.schedule.qry.net.JobNetQry;
import org.ecs.schedule.service.job.CuckooJobService;
import org.ecs.schedule.service.net.CuckooNetService;
import org.ecs.schedule.vo.net.CuckooNetRegistJobVo;
import org.ecs.schedule.web.util.JqueryDataTable;
import org.ecs.util.dao.PageDataList;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/jobclient")
public class JobClientController extends BaseController {

    @Autowired
    private CuckooJobService cuckooJobService;

    @Autowired
    private CuckooNetService cuckooNetService;

    @RequestMapping
    public String index0(HttpServletRequest request) {
        return index(request);
    }

    @RequestMapping(value = "/index")
    public String index(HttpServletRequest request) {
        Map<String, String> jobAppList = cuckooJobService.findAllApps();
        Map<String, String> jobAppWithNull = new HashMap<>();
        jobAppWithNull.put("", "全部/无");
        jobAppWithNull.putAll(jobAppList);
        request.setAttribute("jobAppWithNull", jobAppWithNull);
        return "jobclient/jobclient.index";
    }

    @ResponseBody
    @RequestMapping(value = "/pageList")
    public Object pageList(JobNetQry qry) {
        PageDataList<CuckooNetRegistJob> page = cuckooNetService.pageRegistJob(qry);
        List<CuckooNetRegistJobVo> rows = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(page.getRows())) {
            for (CuckooNetRegistJob r : page.getRows()) {
                CuckooNetRegistJobVo vo = new CuckooNetRegistJobVo();
                BeanUtils.copyProperties(r, vo);
                // 客户端执行器信息
                vo.setClients(getCuckooClients(r));

                // 服务端信息
                vo.setServers(getCuckooServers(r));
                rows.add(vo);
            }
        }

        PageDataList<CuckooNetRegistJobVo> pages = new PageDataList<>();
        pages.setPage(page.getPage());
        pages.setPageSize(page.getPageSize());
        pages.setTotal(page.getTotal());
        pages.setRows(rows);
        return dataTable(pages);
    }

    private Set<String> getCuckooServers(CuckooNetRegistJob job) {
        List<CuckooNetServerInfo> servers = cuckooNetService.getCuckooServersByRegistJob(job);
        Set<String> serverAddrs = servers.stream().map(si -> si.getId() + ":" + si.getIp() + "-" + si.getPort()).collect(Collectors.toSet());
        return serverAddrs;
    }


    private Set<String> getCuckooClients(CuckooNetRegistJob job) {
        List<CuckooNetClientInfo> clients = cuckooNetService.getCuckooClientsByRegistJob(job);
        Set<String> clientAddrs = clients.stream().map(ci -> ci.getIp() + ":" + ci.getClientTag()).collect(Collectors.toSet());
        return clientAddrs;
    }


    /**
     * parse PageDataList to JqueryDataTable
     *
     * @param page
     * @return
     */
    public <T> JqueryDataTable<T> dataTable(PageDataList<T> page) {
        JqueryDataTable<T> t = new JqueryDataTable<>();
        t.setRecordsFiltered(page.getTotal());
        t.setRecordsTotal(page.getTotal());
        t.setData(page.getRows());
        return t;
    }
}
