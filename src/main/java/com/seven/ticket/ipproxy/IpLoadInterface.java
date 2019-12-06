package com.seven.ticket.ipproxy;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.seven.ticket.entity.IP;

import java.util.List;

public interface IpLoadInterface {
    public List<HtmlElement> getHtml();
    public List<IP> loadIpList(List<HtmlElement> elements);
    public void save(List<IP> ips);
}
