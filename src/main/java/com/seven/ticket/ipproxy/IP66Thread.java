package com.seven.ticket.ipproxy;

import com.alibaba.fastjson.JSON;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell;
import com.seven.ticket.config.Constants;
import com.seven.ticket.entity.IP;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: TODO
 * @Author chendongdong
 * @Date 2019/11/20 10:13
 * @Version V1.0
 **/
public class IP66Thread implements Runnable, IpLoadInterface {
    @Override
    public void run() {
        List<HtmlElement> elements = getHtml();
        List<IP> ips = loadIpList(elements);
        save(ips);
    }

    @Override
    public List<HtmlElement> getHtml() {
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        webClient.getOptions().setJavaScriptEnabled(false);
        try {
            HtmlPage page = webClient.getPage(Constants.IP66);
            return page.getByXPath("//*[@id=\"footer\"]/div/table/tbody/tr[position()>1]");
        } catch (IOException e) {
            System.out.println("66IP代理更新失败" + e.getMessage());
        }
        return null;
    }

    @Override
    public List<IP> loadIpList(List<HtmlElement> elements) {
        List<IP> list = new ArrayList<>();
        for (HtmlElement element : elements) {
            HtmlTableDataCell ipCell = element.getFirstByXPath("./td[1]");
            HtmlTableDataCell portCell = element.getFirstByXPath("./td[2]");

            IP ip = new IP(ipCell.getTextContent(), Integer.valueOf(portCell.getTextContent()), "http");
            list.add(ip);
        }
        return list;
    }

    @Override
    public void save(List<IP> ips) {
        for (IP ip : ips) {
            LoadProxy.saveIp(ip);
        }
    }
}
