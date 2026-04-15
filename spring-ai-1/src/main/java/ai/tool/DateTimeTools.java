package ai.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DateTimeTools implements ModelTool {

    @Tool(description = "获取用户所在的地区的时间和时区")
    public String getCurrentDateTime() {
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }


    @Tool(description = "用于在特定时间设置闹钟, 采用 ISO-8601 格式")
    void setAlarm(@ToolParam(description = "ISO-8601 格式时间") String alarmTime) {
        System.out.println("Alarm set for " + alarmTime);
    }
}
