package com.example.gofish;

import android.widget.ImageView;

import java.util.Random;

public class Fish {
    float weight;
    String name;
    String latin;
    int fishImage;

    public Fish(String location) {
        Random r = new Random();

        switch (location) {
            case "Lake":
                if (r.nextFloat() < 0.95) {
                    this.name = "Northern pike";
                    this.latin = "Esox lucius";
                    this.weight = (float)Math.round((5f + 5f*r.nextFloat())*10)/10; // 5 - 10 kg
                } else {
                    this.name = "Gammgäddan";
                    this.latin = "Esox lucius Maximus";
                    this.weight = (float)Math.round((30f + 2.5f*r.nextFloat())*10)/10; // 30 - 32.5 kg
                }
                this.fishImage = R.drawable.pike;
                break;
            case "Dock":
                this.name = "Brown trout";
                this.latin = "salmo trutta";
                this.weight = (float)Math.round((4f + 4f*r.nextFloat())*10)/10; // 4 - 8 kg
                this.fishImage = R.drawable.trout;
                break;
            case "Beach":
                this.name = "European perch";
                this.latin = "Perca fluviatilis";
                this.weight = (float)Math.round((0.5f + 2f*r.nextFloat())*10)/10; // 0.5 - 2.5 kg
                this.fishImage = R.drawable.perch;
                break;
        }
    }

    int getImageResource(){
        return fishImage;
    }

    float getWeight() {
        return weight;
    }

    String getName() {
        return name;
    }

    String getLatinName() {
        return latin;
    }
}
