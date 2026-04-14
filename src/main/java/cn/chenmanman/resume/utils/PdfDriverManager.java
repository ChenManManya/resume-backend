package cn.chenmanman.resume.utils;

import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Component
public class PdfDriverManager {

    private final ChromeDriver chromeDriver;
    private final ReentrantLock lock = new ReentrantLock();

    public PdfDriverManager(ChromeDriver chromeDriver) {
        this.chromeDriver = chromeDriver;
    }

    public <T> T execute(Function<ChromeDriver, T> action) {
        lock.lock();
        try {
            return action.apply(chromeDriver);
        } finally {
            lock.unlock();
        }
    }

    public void executeVoid(java.util.function.Consumer<ChromeDriver> action) {
        lock.lock();
        try {
            action.accept(chromeDriver);
        } finally {
            lock.unlock();
        }
    }
}