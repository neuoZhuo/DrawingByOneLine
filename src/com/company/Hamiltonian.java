package com.company;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class Hamiltonian {
    private enum TAG {
        EMPTY,
        WALL,
        TEMP,
    }

    private static final String[] DIR_TEXT = new String[]{"U,", "R,", "D,", "L,"};
    private static final int[] DIR_OFFSET_Y = new int[]{-1, 0, 1, 0};
    private static final int[] DIR_OFFSET_X = new int[]{0, 1, 0, -1};

    private static class Map {
        TAG[] position;
        int height, width;
        int currentX, currentY;

        void init(int width, int height) {
            this.width = width;
            this.height = height;
            this.position = new TAG[height * width];
            for (int i = 0; i < this.position.length; i++) {
                this.position[i] = TAG.EMPTY;
            }
        }

        void setStart(int x, int y) {
            currentX = x;
            currentY = y;
            set(x, y, TAG.WALL);
        }

        void set(int x, int y, TAG value) {
            this.position[y * width + x] = value;
        }

        boolean isEmpty() {
            for (TAG aPosition : this.position) {
                if (aPosition != TAG.WALL) {
                    return false;
                }
            }
            return true;
        }

        boolean isSuccess() {
            return isEmpty();
        }

        boolean noNext(int x, int y) {
            int stat = 0;
            if (x > 0 && checkEmpty(x - 1, y)) {
                stat ++;
            }
            if (x < width - 1 && checkEmpty(x + 1, y)) {
                stat ++;
            }
            if (y > 0 && checkEmpty(x, y - 1)) {
                stat ++;
            }
            if (y < height - 1 && checkEmpty(x, y + 1)) {
                stat ++;
            }
            return stat == 0;
        }

        boolean isFail() {
            if (noNext(currentX, currentY)) {
                return true;
            }
            if (!isAllArrived()) {
                return true;
            }
            return multiSingle();
        }

        boolean multiSingle() {
            int count = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (!checkEmpty(x, y)) {
                        continue;
                    }

                    int stat = 0;
                    if (x > 0 && (checkEmpty(x - 1, y) || checkCurrent(x - 1, y))) {
                        stat ++;
                    }
                    if (x < width - 1 && (checkEmpty(x + 1, y) || checkCurrent(x + 1, y))) {
                        stat ++;
                    }
                    if (y > 0 && (checkEmpty(x, y - 1) || checkCurrent(x, y - 1))) {
                        stat ++;
                    }
                    if (y < height - 1 && (checkEmpty(x, y + 1) || checkCurrent(x, y + 1))) {
                        stat ++;
                    }
                    if (stat < 2) {
                        count ++;
                    }
                }
            }
            return count > 1;
        }

        boolean checkEmpty(int x, int y) {
            return this.position[y * width + x] == TAG.EMPTY;
        }

        boolean checkCurrent(int x, int y) {
            return x == currentX && y == currentY;
        }

        boolean isAllArrived() {
            dst(currentX, currentY);
            boolean result = true;
            for (int i = 0; i < this.position.length; i++) {
                if (this.position[i] == TAG.EMPTY) {
                    result = false;
                } else if (this.position[i] == TAG.TEMP){
                    this.position[i] = TAG.EMPTY;
                }
            }
            return result;
        }

        void dst(int x, int y) {
            if (y > 0 && checkEmpty(x, y - 1)) {
                set(x, y - 1, TAG.TEMP);
                dst(x, y - 1);
            }
            if (y < height - 1 && checkEmpty(x, y + 1)) {
                set(x, y + 1, TAG.TEMP);
                dst(x, y + 1);
            }
            if (x > 0 && checkEmpty(x - 1, y)) {
                set(x - 1, y , TAG.TEMP);
                dst(x - 1, y);
            }
            if (x < width - 1 && checkEmpty(x + 1, y)) {
                set(x + 1, y, TAG.TEMP);
                dst(x + 1, y);
            }
        }

        boolean notInRange(int x, int y) {
            return  x < 0 || x >= width || y < 0 || y >= height;
        }

        boolean move(int dir) {
            int nextX = currentX + DIR_OFFSET_X[dir];
            int nextY = currentY + DIR_OFFSET_Y[dir];

            if (notInRange(nextX, nextY)) {
                return false;
            }
            if (!checkEmpty(nextX, nextY)) {
                return false;
            }
            currentX = nextX;
            currentY = nextY;

            set(currentX, currentY, TAG.WALL);
            return true;
        }

        void back(int dir) {
            set(currentX, currentY, TAG.EMPTY);

            currentX -= DIR_OFFSET_X[dir];
            currentY -= DIR_OFFSET_Y[dir];
        }
    }

    private static class Reader {
        String[] input;
        Reader(String[] input) {
            this.input = input;
        }

        static final String SEGMENT = ",";
        static final String JOIN = "-";
        Map process() {
            Map map = new Map();
            String[] lines = input;

            String[] first = lines[0].split(SEGMENT);
            map.init(Integer.parseInt(first[0]), Integer.parseInt(first[1]));

            String[] second = lines[1].split(SEGMENT);
            map.setStart(Integer.parseInt(second[0]), Integer.parseInt(second[1]));

            for (int i = 2; i < lines.length; i++) {
                if (lines[i].isEmpty()) {
                    continue;
                }
                String[] currents = lines[i].split(SEGMENT);
                int y = Integer.parseInt(currents[0]);
                for (int j = 1; j < currents.length; j++) {
                    String[] points = currents[j].split(JOIN);
                    int s = Integer.parseInt(points[0]);
                    int e = points.length > 1 ? Integer.parseInt(points[1]) : s;
                    for (int x = s; x <= e; x++) {
                        map.set(x, y, TAG.WALL);
                    }
                }
            }
            return map;
        }
    }

    private Map map;
    private List<Integer> path;

    Hamiltonian load(String filename) {
        File file = new File(filename);
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> lines = new LinkedList<>();
            String line = br.readLine();
            while (line != null) {
                lines.add(line.strip());
                line = br.readLine();
            }
            System.out.println(lines);
            Reader r = new Reader(lines.toArray(new String[0]));
            this.map = r.process();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    private static void outputStep(String step, int count) {
        if (step != null) {
            if (count > 1) {
                System.out.print(count + step);
            } else {
                System.out.print(step);
            }
        }
    }

    void search() {
        path = new LinkedList<>();
        if (!searchInner()) {
            System.out.println("there is no valid path~");
            return;
        }
        Collections.reverse(this.path);

        String last = null;
        int count = 0;

        for (Integer dir : this.path) {
            String current = DIR_TEXT[dir];
            if (current.equals(last)) {
                count ++;
            } else {
                outputStep(last, count);
                count = 1;
                last = current;
            }
        }
        outputStep(last, count);
    }

    private boolean searchInner() {
        if (map.isSuccess()) {
            return true;
        }
        if (map.isFail()) {
            return false;
        }
        for (int dir = 0; dir <= 3; dir++) {
            if (!map.move(dir)) {
                continue;
            }
            if (searchInner()) {
                path.add(dir);
                return true;
            }
            map.back(dir);
        }
        return false;
    }
}
