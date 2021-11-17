package com.arise.core.tools;


import java.util.*;

public class BlockBuilder extends LineBuilder {

    private LineBuilder _h;
    private LineBuilder _f;

    private List<LineBuilder> lines = new ArrayList<>();
    private Map<String, BlockBuilder> blocks = new HashMap<>();

    private String prefix;

    public BlockBuilder(String prefix, String tab) {
        super(tab);
        this.prefix = prefix;
        _h = new LineBuilder(prefix);
        _f = new LineBuilder(prefix);
    }

    public BlockBuilder() {
        this("", "    ");
    }


    public <T> BlockBuilder joinLines(Iterable<String> values, String separator){
        this.join(values, separator, StringUtil.DEFAULT_ITERATOR);
        return this;
    }


    public <T> BlockBuilder join(Iterable<T> values, String separator, StringUtil.JoinIterator<T> iterator){
        int cnt = 0;
        int size = 0;
        if (values instanceof Collection){
            size = ((Collection<T>) values).size();
        } else {
            for (T x: values){
                size++;
            }
        }
        for (T value: values){
            if (cnt < size -1 ){
                writeLine(iterator.toString(value), separator);
            } else {
                writeLine(iterator.toString(value));
            }
            cnt++;
        }
        return this;
    }

    @Override
    public LineBuilder endl() {
        lines.add(new LineBuilder(prefix).endl());
        return this;
    }

    @Override
    public BlockBuilder writeLine(String... args) {
        lines.add(new LineBuilder(prefix).writeLine(args));
        return this;
    }

    public BlockBuilder getBlock(String id){
        if (!blockExists(id)){
            blocks.put(id, new BlockBuilder(prefix + tab, tab));
            lines.add(blocks.get(id));
        }
        return blocks.get(id);
    }



    public boolean blockExists(String id){
        return blocks.containsKey(id);
    }





    public LineBuilder header(){
        return _h;
    }


    public LineBuilder footer(){
        return _f;
    }

    @Override
    public String toString() {
        List<LineBuilder> copy = new ArrayList<>();
        copy.add(_h);
        for (LineBuilder s: this.lines){
            copy.add(s);
        }
        copy.add(_f);
        return joinLines(copy);
    }

    private String joinLines(List<LineBuilder> lines){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < lines.size(); i++){
            LineBuilder lb = lines.get(i);
            sb.append(lb.toString());
        }
        return sb.toString();
    }

    public void writeBlockNodes(String[] names) {
        if (names != null && names.length > 0){
            getBlock(names[0]).writeBlockNodes(Arrays.copyOfRange(names, 1, names.length));
        }
    }

    public BlockBuilder getBlockNode(String[] names) {
        if (names != null && names.length > 0){
            return getBlock(names[0]).getBlockNode(Arrays.copyOfRange(names, 1, names.length));
        }
        return this;
    }

    public BlockBuilder getBlockNodeAt(int i, String[] names) {
        if (names != null && names.length > 0 && i > -1){



            BlockBuilder current = blocks.get(names[0]);
            if (i == 0){
                return current;
            }
            return current.getBlockNodeAt(i -1,  Arrays.copyOfRange(names, 1, names.length));
        }
        return this;
    }

    //    public BlockBuilder(String prefix, String tab, T builder) {
//        super(prefix + tab, builder);
//        _h = new BlockBuilder<T>(builder)
//    }




    //    Block getBlock(String id){
//        if (!blocks.containsKey(id)){
//            blocks.put(id, new Block());
//            stringBuilders.add(blocks.get(id));
//        }
//        return blocks.get(id);
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        for (Block b: stringBuilders){
//            sb.append(b.toString());
//        }
//        return sb.toString();
//    }




//    class Block {
//
//        public StringBuilder start() {
//            return _s;
//        }
//
//        public StringBuilder payload() {
//            return _p;
//        }
//
//        public StringBuilder end() {
//            return _e;
//        }
//
//        StringBuilder _s = new StringBuilder();
//        StringBuilder _p = new StringBuilder();
//        StringBuilder _e = new StringBuilder();
//
//        @Override
//        public String toString() {
//            return _s.toString() + _p.toString() + _e.toString();
//        }
//    }
}
