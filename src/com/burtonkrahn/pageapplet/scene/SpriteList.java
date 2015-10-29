package com.burtonkrahn.pageapplet.scene;

import java.lang.*;
import java.util.*;
import java.awt.*;

public class SpriteList 
{
    java.util.List<Sprite> spriteList;
    Dimension spriteSize;

    public SpriteList() {
        this.spriteList = new java.util.ArrayList<Sprite>();
    }

    public Sprite get(int index) {
        return this.spriteList.get(index);
    }

    public void set(int index, Sprite sprite) {
        this.spriteList.set(index, sprite);
    }

    public void add(Sprite sprite) {
        this.spriteList.add(sprite);
    }

    public int size() {
        return this.spriteList.size();
    }

    public Dimension getSpriteSize() {
        if( this.spriteSize == null ) {
            return get(0).getSize();
        }
	return this.spriteSize;
    }

    public void setSpriteSize(Dimension spriteSize) {
	this.spriteSize = spriteSize;
    }
}
