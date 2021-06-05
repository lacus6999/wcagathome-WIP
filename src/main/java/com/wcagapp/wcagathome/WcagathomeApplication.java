package com.wcagapp.wcagathome;

import com.wcagapp.wcagathome.spectrumrenderer.SpectrumRenderer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class WcagathomeApplication implements CommandLineRunner {


    public static void main(String[] args) {
        SpringApplication.run(WcagathomeApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
    }
}
