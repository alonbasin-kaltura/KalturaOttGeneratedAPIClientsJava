package com.kaltura.client.test.utils;

import com.kaltura.client.ILogger;
import com.kaltura.client.Logger;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

import static com.kaltura.client.test.Properties.*;

public class PerformanceUtils extends BaseUtils {

    private static final ILogger logger = Logger.getLogger(PerformanceUtils.class);

    private static final int maxAllowedPercentage = Integer.parseInt(getProperty(MAX_CODE_PERCENTAGE));
    private static final float maxAllowedTime = Float.parseFloat(getProperty(MAX_EXECUTION_TIME_IN_SEC));

    private static final String logsUrl = getProperty(LOGS_BASE_URL) + getProperty(API_VERSION) + "/";
    private static final String filesCssQuery = "a:contains(tvp-api-rest-monitor-" + getProperty(API_VERSION) + ")";
    private static final String reportFilePath = getProperty(LOGS_DIR) + getProperty(PERFORMANCE_REPORT_FILE);


    public static void generatePerformanceReport() {
        logger.debug("start generatePerformanceReport()");

        // get aggregate regression sessions strings
        List<String> regressionSessions = getRegressionData().values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // get sessions
        List<Session> sessions = getSessions(regressionSessions);

        // write performance report
        writeReport(sessions);

        logger.debug("finish generatePerformanceReport()");
    }

    private static Map<String, List<String>> getRegressionData() {
        logger.debug("start getRegressionData()");

        File data = new File(getProperty(LOGS_DIR) + getProperty(REGRESSION_LOGS_FILE));
        List<String> lines = null;
        try {
            lines = Files.readAllLines(data.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.debug("end getRegressionData()");

        return lines
                .stream()
                .collect(Collectors.groupingBy(
                        s -> s.split(":")[0],
                        Collectors.mapping(s -> s.split(":")[1],
                        Collectors.toList())));
    }

    private static List<URL> getLogFilesUrls() {
        logger.debug("start getLogFilesUrls()");

        Document doc = null;
        try {
            doc = Jsoup.connect(logsUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
            File file = getReportFile();
            try {
                FileUtils.writeStringToFile(file, ExceptionUtils.getStackTrace(e), Charset.defaultCharset(), true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        List<URL> urls = new ArrayList<>();

        doc.select(filesCssQuery).forEach(element -> {
            try {
                URL url = new URL(logsUrl + element.text());
                urls.add(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });

        logger.debug("end getLogFilesUrls()");

        return urls;
    }

    private static List<String> getLinesFromUrls(List<URL> urls) {
        logger.debug("start getLinesFromUrls()");

        List<List<String>> data = new ArrayList<>();

        urls.forEach(url -> {
            URLConnection conn = null;
            try {
                conn = url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                List<String> lines = reader.lines().collect(Collectors.toList());
                data.add(lines);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        logger.debug("end getLinesFromUrls()");

        return data
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private static List<Session> getSessions(List<String> sessionStrings) {
        logger.debug("start getSessions()");

        List<String> lines = getLinesFromUrls(getLogFilesUrls());
       
        List<Session> sessionList = new ArrayList<>();
        sessionStrings.forEach(s -> {
            Session session = getSession(lines, s);
            if (session != null) {
                sessionList.add(session);
            }
        });

        logger.debug("end getSessions()");
        return sessionList;
    }

    private static Session getSession(List<String> lines, String session) {
        List<JSONObject> sessionData = new ArrayList<>();

        lines.forEach(line -> {
            if (line.contains(session)) {
                JSONObject jo = new JSONObject(line.substring(line.indexOf("{")));
                sessionData.add(jo);
            }
        });

        if (sessionData.size() == 0) {
//            logger.debug("missing session from logs: " + session);
            return null;
        }

        return new Session(sessionData);
    }

    private static List<Session> getSlowSessions(List<Session> sessions) {
        return sessions
                .stream()
                .filter(Session::isSlow)
                .collect(Collectors.toList());
    }

    private static File getReportFile() {
        File file = new File(reportFilePath);

        if (file.exists()) {
            file.delete();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private static void writeReport(List<Session> sessions) {
        logger.debug("start writeReport()");

        // get slow sessions
        List<Session> slowSessions = getSlowSessions(sessions);

        // slow actions by count
        Map<String, Long> slowActionsByCount = slowSessions.stream().collect(Collectors.groupingBy(
                Session::getAction,
                Collectors.counting()
        ));

        // actions by count
        Map<String, Long> actionsByCount = sessions.stream().collect(Collectors.groupingBy(
                Session::getAction,
                Collectors.counting()
        ));

        // actions by average time
        Map<String, Double> slowActionsByAverageTime = new HashMap<>();
        slowActionsByCount.keySet().forEach(s -> {
            long actionTotalCount = actionsByCount.get(s);

            double averageTime = sessions.stream()
                    .filter(session -> session.getAction().equals(s))
                    .mapToDouble(Session::getTotalTime).sum() / actionTotalCount;

            slowActionsByAverageTime.put(s, averageTime);
        });

        // sort actions by average time
        List<Map.Entry<String, Double>> slowActionsAverageTimeSortedList = slowActionsByAverageTime.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        // write data to file
        File file = getReportFile();

        String reportSummary = "Performance Report - " + getCurrentDateInFormat("dd/MM/yyyy HH:mm") + " (" + getProperty(API_VERSION) + ")\n"
                + "Max percentage: " + getProperty(MAX_CODE_PERCENTAGE) + "%\n"
                + "Max execution time: " + getProperty(MAX_EXECUTION_TIME_IN_SEC) + " sec\n\n"
                + "Slow Actions Summary:\n\n";
        try {
            FileUtils.writeStringToFile(file, reportSummary, Charset.defaultCharset(), true);

            slowActionsAverageTimeSortedList.forEach(entry -> {
                long actionSlowCount = slowActionsByCount.get(entry.getKey());
                long actionTotalCount = actionsByCount.get(entry.getKey());
                double slowPercentage = (double) actionSlowCount / actionTotalCount * 100;
                double averageTime = entry.getValue();

                try {
                    FileUtils.writeStringToFile(file, entry.getKey() + " - was slow " + String.format("%.0f", slowPercentage) +
                                    "% of executions (" + actionSlowCount + "/" + actionTotalCount + ") " + "[average time: " + String.format("%.2f", averageTime) + " sec]\n",
                            Charset.defaultCharset(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            FileUtils.writeStringToFile(file, "\nSlow Actions Details: \n\n", Charset.defaultCharset(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        slowSessions.forEach(session -> {
            try {
                FileUtils.writeStringToFile(file, session.toString() + "\n", Charset.defaultCharset(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        logger.debug("end writeReport()");
    }

    @Getter
    private static class Session {
        @Getter(AccessLevel.NONE) private final String numFormat = "%.3f";
        @Getter(AccessLevel.NONE) private final String newLine = "\n";

        @Getter(AccessLevel.NONE) private final String CB_EVENT = "cb";
        @Getter(AccessLevel.NONE) private final String DB_EVENT = "db";
        @Getter(AccessLevel.NONE) private final String ES_EVENT = "elastic";
        @Getter(AccessLevel.NONE) private final String RABBIT_EVENT = "rabbit";
        @Getter(AccessLevel.NONE) private final String WS_EVENT = "ws";

        @Getter(AccessLevel.NONE) private final String EVENT_KEY = "e";
        @Getter(AccessLevel.NONE) private final String EXECUTION_TIME_KEY = "x";

        private String action;
        private String session;

        private boolean isSlow;

        private long startTime;
        private long endTime;

        private double totalTimeExcludeCode;
        private double totalTime;
        private double codeTime;
        private double cbTime;
        private double dbTime;
        private double esTime;
        private double rabbitTime;
        private double wsTime;
        private double codePercentage;

        private int cbCount;
        private int dbCount;
        private int esCount;
        private int wsCount;
        private int rabbitCount;

        Session(List<JSONObject> sessionData) {
            sessionData.forEach(line -> {
                if (!line.getString(EVENT_KEY).equals("start_api") && !line.getString(EVENT_KEY).equals("end_api")) {
                    totalTimeExcludeCode = totalTimeExcludeCode + line.getFloat(EXECUTION_TIME_KEY);
                }

                if (line.getString(EVENT_KEY).equals("start_api")) {
                    startTime = line.getLong("m");
                }

                if (line.getString(EVENT_KEY).equals("end_api")) {
                    endTime = line.getLong("m");
                }

                if (line.getString(EVENT_KEY).equals(CB_EVENT)) {
                    cbTime = cbTime + line.getDouble(EXECUTION_TIME_KEY);
                    cbCount++;
                }

                if (line.getString(EVENT_KEY).equals(DB_EVENT)) {
                    dbTime = dbTime + line.getDouble(EXECUTION_TIME_KEY);
                    dbCount++;
                }

                if (line.getString(EVENT_KEY).equals(ES_EVENT)) {
                    esTime = esTime + line.getDouble(EXECUTION_TIME_KEY);
                    esCount++;
                }

                if (line.getString(EVENT_KEY).equals(RABBIT_EVENT)) {
                    rabbitTime = rabbitTime + line.getDouble(EXECUTION_TIME_KEY);
                    rabbitCount++;
                }

                if (line.getString(EVENT_KEY).equals(WS_EVENT)) {
                    wsTime = wsTime + line.getDouble(EXECUTION_TIME_KEY);
                    wsCount++;
                }
            });

            action = sessionData.get(0).getString("a");
            session = sessionData.get(0).getString("u");

            totalTime = (double) (endTime - startTime) / 1000000;
            codeTime = totalTime - totalTimeExcludeCode;
            codePercentage = codeTime / totalTime * 100;


            if ((totalTime > maxAllowedTime) &&
                    (codePercentage > maxAllowedPercentage) &&
                    (cbTime > 0 || dbTime > 0 || esTime > 0 || rabbitTime > 0 || wsTime > 0)) {
                isSlow = true;
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder()
                    .append("action: ").append(this.getAction()).append(newLine)
                    .append("session: ").append(this.getSession()).append(newLine)
                    .append("total time: ").append(String.format(numFormat, this.getTotalTime())).append(newLine)
                    .append("code: ").append(String.format(numFormat, this.getCodePercentage())).append("% (" + String.format(numFormat, this.getCodeTime()) + ")").append(newLine);

            writeIfEventTimeGreaterThanZero(sb, this.getCbTime(), this.getCbCount(), this.CB_EVENT);
            writeIfEventTimeGreaterThanZero(sb, this.getDbTime(), this.getDbCount(), this.DB_EVENT);
            writeIfEventTimeGreaterThanZero(sb, this.getEsTime(), this.getEsCount(), this.ES_EVENT);
            writeIfEventTimeGreaterThanZero(sb, this.getRabbitTime(), this.getRabbitCount(), this.RABBIT_EVENT);
            writeIfEventTimeGreaterThanZero(sb, this.getWsTime(), this.getWsCount(), this.WS_EVENT);

            return sb.toString();
        }

        private void writeIfEventTimeGreaterThanZero(StringBuilder sb, double eventTime, int eventCount, String eventName) {
            if (eventTime > 0) {
                sb
                        .append(eventName + ": ")
                        .append(String.format(numFormat, eventTime / this.getTotalTime() * 100))
                        .append("% (" + String.format(numFormat, eventTime) + ")")
                        .append(" [" + eventCount + " queries]")
                        .append("\n");
            }
        }
    }
}
