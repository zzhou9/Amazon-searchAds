package io.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.net.*;

//import org.apache.log4j.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import io.ad.Ad;

/**
 * 
 */


public class AmazonCrawler {
    private static final String AMAZON_QUERY_URL
            = "https://www.amazon.com/s/ref=nb_sb_noss?field-keywords=";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36"
            + " (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36";
    private final String authUser = "85817";
    private final String authPassword = "tQpR6NS9";
    private List<String> proxyList;
    private List<String> titleList;
    private List<String> categoryList;
    BufferedWriter logBFWriter;

    private int index = 0;
    private int id = 2000;

    public AmazonCrawler(String proxyFile, String logFile) {
        initProxyList(proxyFile);

        initHtmlSelector();

        initLog(logFile);

    }

    public void cleanup() {
        if (logBFWriter != null) {
            try {
                logBFWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initProxyList(String proxyFile) {
        proxyList = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(proxyFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );

        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("socksProxyPort", "3128"); // set proxy port
    }

    private void initHtmlSelector() {
        titleList = new ArrayList<String>();
        titleList.add(" > div:nth-child(1) > div:nth-child(1) > div:nth-child(1) > div:nth-child(2) > "
                    + "div:nth-child(3) > div:nth-child(1) > a:nth-child(1) > h2:nth-child(1)");
        titleList.add(" > div > div > div > div.a-fixed-left-grid-col.a-col-right > "
                        + "div.a-row.a-spacing-small > div:nth-child(1) > a > h2");

        categoryList = new ArrayList<String>();
        categoryList.add("ul.a-spacing-base:nth-child(2) > div:nth-child(1) > li:nth-child(1) > "
                       + "span:nth-child(1) > a:nth-child(1) > h4:nth-child(1)");
    }

    private void initLog(String logPath) {
        try {
            File log = new File(logPath);
            // if file doesnt exists, then create it
            if (!log.exists()) {
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
            logBFWriter = new BufferedWriter(fw);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setProxy() {
        //rotate
        if (index == proxyList.size()) {
            index = 0;
        }
        String proxy = proxyList.get(index);
        System.setProperty("socksProxyHost", proxy); // set proxy server
        index++;
    }

    private void testProxy() {
        //System.setProperty("socksProxyHost", "89.32.71.32"); // set proxy server
        //System.setProperty("socksProxyPort", "3128"); // set proxy port
        String testUrl = "http://www.toolsvoid.com/what-is-my-ip-address";

        try {
            Document doc = Jsoup.connect(testUrl).userAgent(USER_AGENT).timeout(10000).get();
            String iP = doc.select("body > section.articles-section > div > div > div > "
                    + "div.col-md-8.display-flex > div > div.table-responsive > table > tbody > "
                    + "tr:nth-child(1) > td:nth-child(2) > strong").first().text(); //get used IP.
            System.out.println("IP-Address: " + iP);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public List<Ad> getAdBasicInfoByQuery(String query, double bidPrice, int campaignId, int queryGroupId) {
        List<Ad> products = new ArrayList<>();
        try {
            //testProxy();
            //setProxy();

            String real_query = query.replaceAll("\\s", "%20");
            String url = AMAZON_QUERY_URL + real_query;

            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
            headers.put("Accept-Language", "en-US,en;q=0.8");
            Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).timeout(100000).get();
            //Document doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(100000).get();

            System.out.println(url);

            //System.out.println(doc.text());
            //#s-results-list-atf
            //Elements results = doc.select("#s-results-list-atf").select("li");
            //System.out.println(doc.select("#s-results-list-atf").html());
            Elements results = doc.select("li[data-asin]");
            System.out.println("num of results = " + results.size());
            for (int i = 0; i < results.size(); i++) {
                Ad ad = new Ad();
                //ad.adId = id++;
                ad.query = query;
                ad.queryGroupId = queryGroupId;
                ad.keyWords = new ArrayList<>();
                //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
                //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
                for (String title : titleList) {
                    String titleElePath = "#result_" + Integer.toString(i) + title;
                    Element titleEle = doc.select(titleElePath).first();
                    if (titleEle != null) {
                        System.out.println("title = " + titleEle.text());
                        ad.title = titleEle.text();
                        break;
                    }
                }

                if (ad.title == null || ad.title.equals("")) {
                    logBFWriter.write("cannot parse title for query: " + query);
                    logBFWriter.newLine();
                    continue;
                }

                String thumbnailPath = "#result_" + Integer.toString(i) + " > div > div > div > "
                                     + "div.a-fixed-left-grid-col.a-col-left > div > div > a > img";
                Element thumbnailEle = doc.select(thumbnailPath).first();
                if (thumbnailEle != null) {
                    //System.out.println("thumbnail = " + thumbnail_ele.attr("src"));
                    ad.thumbnail = thumbnailEle.attr("src");
                } else {
                    logBFWriter.write("cannot parse thumbnail for query:" + query
                                        + ", title: " + ad.title);
                    logBFWriter.newLine();
                    continue;
                }

                String detailPath = "#result_" + Integer.toString(i) + " > div > div > div > "
                                  + "div.a-fixed-left-grid-col.a-col-right > div > div > a";
                Element detailUrlEle = doc.select(detailPath).first();
                if (detailUrlEle != null) {
                    String detailUrl = detailUrlEle.attr("href");
                    //System.out.println("detail = " + detail_url);
                    ad.detailUrl = detailUrl;
                } else {
                    logBFWriter.write("cannot parse detail for query:" + query + ", title: " + ad.title);
                    logBFWriter.newLine();
                    continue;
                }


                String brandPath = "#result_" + Integer.toString(i)
                        + " > div > div > div > div.a-fixed-left-grid-col.a-col-right "
                        + "> div.a-row.a-spacing-small > div > span:nth-child(2)";
                Element brand = doc.select(brandPath).first();
                if (brand != null) {
                    //System.out.println("brand = " + brand.text());
                    ad.brand = brand.text();
                }
                //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > span
                ad.bidPrice = bidPrice;
                ad.campaignId = campaignId;
                ad.price = 0.0;

                String priceWholePath = "#result_" + Integer.toString(i) + "> div > div > div > "
                        + "div.a-fixed-left-grid-col.a-col-right > div > div.a-column.a-span7 "
                        + "> div.a-row.a-spacing-none > a > span > span > span";
                String priceFractionPath = "#result_" + Integer.toString(i)
                        + "> div > div > div > div.a-fixed-left-grid-col.a-col-right > div > "
                        + "div.a-column.a-span7 > div.a-row.a-spacing-none > a > span >"
                        + " span > sup.sx-price-fractional";
                Element priceWholeEle = doc.select(priceWholePath).first();
                if (priceWholeEle != null) {
                    String priceWhole = priceWholeEle.text();
                    //System.out.println("price whole = " + priceWhole);
                    //remove ","
                    //1,000
                    if (priceWhole.contains(",")) {
                        priceWhole = priceWhole.replaceAll(",", "");
                    }

                    try {
                        ad.price = Double.parseDouble(priceWhole);
                    } catch (NumberFormatException ne) {
                        // TODO Auto-generated catch block
                        ne.printStackTrace();
                        //log
                    }
                }

                Element priceFractionEle = doc.select(priceFractionPath).first();
                if (priceFractionEle != null) {
                    //System.out.println("price fraction = " + price_fraction_ele.text());
                    try {
                        ad.price = ad.price + Double.parseDouble(priceFractionEle.text()) / 100.0;
                    } catch (NumberFormatException ne) {
                        ne.printStackTrace();
                    }
                }
                //System.out.println("price = " + ad.price );

                //category
                for (String category : categoryList) {
                    Element categoryEle = doc.select(category).first();
                    if (categoryEle != null) {
                        //System.out.println("category = " + categoryEle.text());
                        ad.category = categoryEle.text();
                        break;
                    }
                }
                if (ad.category == null || ad.category.equals("")) {
                    logBFWriter.write("cannot parse category for query:"
                                       + query + ", title: " + ad.title);
                    logBFWriter.newLine();
                    continue;
                }
                products.add(ad);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return products;
    }
}
