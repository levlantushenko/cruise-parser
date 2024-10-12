package org.example;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class VolgaVolgaParser {

    public static void parseVolgaVolgaCruises(String shipName, List<String> urls) throws Exception {

        WebDriver driver;
        ChromeOptions options = new ChromeOptions();
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(3));
        driver.manage().window().maximize();
        List<VolgaVolgaData> data = new ArrayList<>();
        try {

            for (String url : urls) {
                driver.get(url);
                System.out.println("Website opened");
                driver.findElement(By.xpath("//*[@href='#t_5']")).click();
                List<WebElement> rows = driver.findElements(By.className("product-row"));
                for (WebElement row : rows) {

                    String stops = row.findElement(By.className("kruiz-ways")).getText()
                            .replace("\u2013", "_")
                            .replace(" _ ", "_")
                            .replace(" - ", "_")
                            .replace(" + ", "_");
                    String link = row.findElement(By.xpath("./*[@id='th-info']/a")).getAttribute("href");
                    VolgaVolgaData dataRow = new VolgaVolgaData();
                    dataRow.shipName = shipName;
                    dataRow.link = link;
                    dataRow.stops = stops;
                    data.add(dataRow);
                }
            }
            loadDates(data, driver);
            writeToFile(data);
        }finally {

            driver.close();
        }
    }

    private static void loadDates(List<VolgaVolgaData> data, WebDriver driver) throws Exception {
        for (VolgaVolgaData row: data){
            driver.get(row.link);
            String startDate = driver.findElement(By.xpath("//*[contains(text(), '����������� ���������:')]/..")).getText();
            startDate = startDate.replace("����������� ���������:", "").trim();
            row.startDate = parseDate(startDate);

            String endDate = driver.findElement(By.xpath("//*[contains(text(), '�������� ���������:')]/..")).getText();
            endDate = endDate.replace("�������� ���������:", "").trim();
            row.endDate = parseDate(endDate);

            System.out.println(startDate + " - " + endDate);
        }
    }

    private static void writeToFile(List<VolgaVolgaData> data) throws Exception {
        Writer textFile = new OutputStreamWriter(new FileOutputStream("./result.txt"), StandardCharsets.UTF_8);
        try{
            SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            for(VolgaVolgaData row: data) {
                textFile.write(row.shipName + "\t" + format.format(row.startDate)
                        + "\t" + format.format(row.endDate)
                        + "\t" + row.stops
                        + "\t" + row.link
                        + "\n");
            }
        }finally {
            textFile.close();
        }
    }

    private static class VolgaVolgaData {
        public String shipName;
        public String stops;
        public String link;
        public Date startDate;
        public Date endDate;
    }
    private static Date parseDate(String date) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        String dateTrimmed = date.substring(0, date.indexOf("�.") - 1);
        return format.parse(dateTrimmed);
    }
}