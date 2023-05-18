package com.example.project;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
public class api {
    public static void main(String[] args){
        try {
            double[] q1 = Q1();
            for (double q: q1){
                System.out.println(q);
            }
            double[] q2 = Q2();
            for (double q: q2){
                System.out.println(q);
            }
            Q3();
            Q4();
            adv();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    /*
    Number of answers:
    double 数组
    a. zero
    b. 1-3
    c. >3
    d. avg
    e. max
     */
    public static double[] Q1()throws IOException{
        List<JSONObject> questions = readJson("data.json");
        double a = 0,b = 0,c = 0,d = 0,e = 0;
        for (int i = 0;i<500;i++){
            int num = Integer.parseInt(questions.get(i).get("answer_count").toString());
            if (num == 0){
                a++;
            }else {
                if (num>e){
                    e = num;
                }
                if (num <= 3){
                    b++;
                }else{
                    c++;
                }
                d += num;
            }

        }
        d /= 500;
        double[] ans = new double[5];
        ans[0] = a;
        ans[1] = b;
        ans[2] = c;
        ans[3] = d;
        ans[4] = e;
    return ans;
    }
    /*
    Accept answers:
    double 数组
    a. 没有accept的个数
    b. 1day
    c. 3day
    d. 7day
    e.>7day
    f.不是接受的答案的upvote>接受的；(前提是已经有accepted answer:138个中有80个满足情况)
     */
    public static double[] Q2()throws IOException{
        List<JSONObject> questions = readJson("data.json");
        double[] ans = new double[6];
        double a=0,b=0,c=0,d=0,e=0,f=0;
        for (JSONObject question : questions) {
            if (Integer.parseInt(question.get("answer_count").toString()) > 0) {
                JSONArray answers = question.getJSONArray("answers");
                long start_time = question.getLong("creation_date");
                int flag = 0;
                for (int i = 0;i<answers.length();i++){
                    JSONObject answer = answers.getJSONObject(i);
                    int up_accept = 0;
                    int max_up_unaccept = 0;

                    if (answer.get("is_accepted").toString().equals("true")){
                        flag = 1;
                        max_up_unaccept = Integer.parseInt(answer.get("up_vote_count").toString());
                        long end_time = answer.getLong("creation_date");
                        Duration duration = Duration.between(Instant.ofEpochSecond(start_time), Instant.ofEpochSecond(end_time));
                        long days = duration.toDays();
                        long hours = duration.toHours() % 24;
                        long minutes = duration.toMinutes() % 60;
                        long seconds = duration.getSeconds() % 60;
                        if (days == 0){
                            b++;
                        }else if (days <= 3){
                            c++;
                        }else if (days <=7){
                            d++;
                        }else {
                            e++;
                        }
                    }else {
                        if (Integer.parseInt(answer.get("up_vote_count").toString())>max_up_unaccept){
                            max_up_unaccept = Integer.parseInt(answer.get("up_vote_count").toString());
                        }
                    }
                    if (max_up_unaccept > up_accept && flag == 0){
                        f++;
                    }
                }
                if (flag == 0){
                    a++;
                }
            }else {
                a++;
            }
        }
        ans[0] = a;ans[1] = b;ans[2] = c;ans[3] = d;ans[4] = e;ans[5] = f;
        return ans;
    }

    /*
    Tags:
    List<HashMap.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());最常出现的 转成 List 从大到小排序
    List<String> vote_max:
    List<String> max_view:
     */
    public static void Q3()throws IOException{
        List<JSONObject> questions = readJson("data.json");
        HashMap<String,Integer> map = new HashMap<>();

        List<String> vote_max = null;
        List<String> view_max = null;
        HashMap<List<String>,Integer> vote_map = new HashMap<>();
        HashMap<List<String>,Integer> view_map = new HashMap<>();

        for (JSONObject question : questions) {
            JSONArray tags = question.getJSONArray("tags");
            List<String> votes = new ArrayList<>();
            List<String> views = new ArrayList<>();

            for (int i = 0;i<tags.length();i++){
                String tag = tags.get(i).toString();
                votes.add(tags.get(i).toString());
                views.add(tags.get(i).toString());
                if (!tag.equals("java")){
                    map.put(tag,map.getOrDefault(tag,0) + 1);
                }
            }
            Collections.sort(votes);
            Collections.sort(views);
            vote_map.put(votes,vote_map.getOrDefault(votes,0)+Integer.parseInt(question.get("up_vote_count").toString()));
            view_map.put(views,view_map.getOrDefault(views,0)+Integer.parseInt(question.get("view_count").toString()));
        }
        List<HashMap.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));


        int max = 0;
        for (List<String> key:vote_map.keySet()){
            if (vote_map.get(key)>max){
                vote_max = key;
                max = vote_map.get(key);
            }
        }
        max = 0;
        for (List<String> key:view_map.keySet()){
            if (view_map.get(key)>max){
                view_max = key;
                max = view_map.get(key);
            }
        }


        //打印结果、、、、、、、、、、、、、、、、、
        for (int i = 0;i<5;i++){
            System.out.println(entryList.get(i));
        }
        System.out.println(vote_max);
        System.out.println(view_max);
//        for (Map.Entry<String, Integer> entry : entryList) {
//            System.out.println(entry.getKey() + ": " + entry.getValue());
//        }
//        System.out.println(votes);
//        System.out.println(views);

    }

    /*
    Users:
    先用user_id 假如没有，就用display_name
    hashmap  map:<string,int> 最常出现的user_id
    hashmap  id_name<string,string> user_id -> display_name
    *1.map转成了List排序：entryList  a=b的格式,a可以取id_name里找对应的name，要不然a就是默认user_id

    1个(answer and commit 是0) <=3  <=5   <=10  >10
    2.int数组 all:
    3.int数组 answer_num:
    4.int数组 comment_num:
     */
    public static void Q4()throws IOException{
        List<JSONObject> questions = readJson("data.json");
        HashMap<String,Integer> map = new HashMap<>();
        HashMap<String,String> id_name = new HashMap<>();
        int[] all = new int[5];
        int[] answer_num = new int[5];
        int[] comment_num = new int[5];
        for (int i = 0;i<all.length;i++){
            all[i] = 0;
            answer_num[i] = 0;
            comment_num[i] = 0;
        }
        for (JSONObject question : questions) {
            Set<String> all_user_id = new HashSet<>();
            Set<String> answer_user_id = new HashSet<>();
            Set<String> comment_user_id = new HashSet<>();
            JSONObject thread_owner = question.getJSONObject("owner");

            String thread_user_id;
            if (thread_owner.isNull("user_id")){
                thread_user_id = thread_owner.get("display_name").toString();
            }else {
                thread_user_id = thread_owner.get("user_id").toString();
                id_name.put(thread_user_id,thread_owner.get("display_name").toString());
            }

            all_user_id.add(thread_user_id);
            if (!map.containsKey(thread_user_id)){
                map.put(thread_user_id,1);
            }else {
                map.replace(thread_user_id,map.get(thread_user_id),map.get(thread_user_id)+1);
            }
            if (Integer.parseInt(question.get("comment_count").toString())>0){
                JSONArray comments = question.getJSONArray("comments");
                for (int i = 0;i<comments.length();i++){
                    JSONObject comment = comments.getJSONObject(i);
                    JSONObject owner = comment.getJSONObject("owner");
                    String user_id;
                    if (owner.isNull("user_id")){
                        user_id = owner.get("display_name").toString();
                    }else {
                        user_id= owner.get("user_id").toString();
                        id_name.put(user_id,owner.get("display_name").toString());
                    }
                    if (!map.containsKey(user_id)){
                        map.put(user_id,1);
                    }else {
                        map.replace(user_id,map.get(user_id),map.get(user_id)+1);
                    }
                    comment_user_id.add(user_id);
                    all_user_id.add(user_id);
                }
            }
            if (Integer.parseInt(question.get("answer_count").toString())>0){
                JSONArray answers = question.getJSONArray("answers");
                for (int i = 0;i<answers.length();i++){
                    JSONObject answer = answers.getJSONObject(i);
                    JSONObject owner = answer.getJSONObject("owner");
                    String user_id;
                    if (owner.isNull("user_id")){
                        user_id = owner.get("display_name").toString();
                    }else {
                        user_id= owner.get("user_id").toString();
                        id_name.put(user_id,owner.get("display_name").toString());
                    }
                    if (!map.containsKey(user_id)){
                        map.put(user_id,1);
                    }else {
                        map.replace(user_id,map.get(user_id),map.get(user_id)+1);
                    }
                    answer_user_id.add(user_id);
                    all_user_id.add(user_id);
                    if (Integer.parseInt(answer.get("comment_count").toString())>0){
                        JSONArray comments = answer.getJSONArray("comments");
                        for (int j = 0;j<comments.length();j++){
                            JSONObject comment = comments.getJSONObject(j);
                            JSONObject owner_comment = comment.getJSONObject("owner");
                            String user_id_comment;
                            if (owner_comment.isNull("user_id")){
                                user_id_comment = owner_comment.get("display_name").toString();
                            }else {
                                user_id_comment = owner_comment.get("user_id").toString();
                                id_name.put(user_id_comment,owner_comment.get("display_name").toString());
                            }
                            if (!map.containsKey(user_id_comment)){
                                map.put(user_id_comment,1);
                            }else {
                                map.replace(user_id_comment,map.get(user_id_comment),map.get(user_id_comment)+1);
                            }
                            comment_user_id.add(user_id_comment);
                            all_user_id.add(user_id_comment);
                        }
                    }
                }
            }
            if (all_user_id.size()==1){
                all[0] ++;
            } else if (all_user_id.size()<=3){
                all[1] ++;
            }else if (all_user_id.size()<=5){
                all[2] ++;
            }else if (all_user_id.size()<=10){
                all[3] ++;
            }else {
                all[4] ++;
            }

            if (answer_user_id.size()==0){
                answer_num[0] ++;
            } else if (answer_user_id.size()<=3){
                answer_num[1] ++;
            }else if (answer_user_id.size()<=5){
                answer_num[2] ++;
            }else if (answer_user_id.size()<=10){
                answer_num[3] ++;
            }else {
                answer_num[4] ++;
            }

            if (comment_user_id.size()==0){
               comment_num[0] ++;
            } else if (comment_user_id.size()<=3){
                comment_num[1] ++;
            }else if (comment_user_id.size()<=5){
                comment_num[2] ++;
            }else if (comment_user_id.size()<=10){
                comment_num[3] ++;
            }else {
                comment_num[4] ++;
            }

        }
        List<HashMap.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
// 打印结果、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、、


        for (int i = 0;i<3;i++) {
            System.out.println(id_name.getOrDefault(entryList.get(i).toString().split("=")[0]
                    ,entryList.get(i).toString().split("=")[0]) + ": " + entryList.get(i).toString().split("=")[1]);
        }

        for (int i = 0;i<all.length;i++){
            System.out.print(all[i] + " ");
        }
        System.out.println();
        for (int i = 0;i<all.length;i++){
            System.out.print(answer_num[i] + " ");
        }
        System.out.println();
        for (int i = 0;i<all.length;i++){
            System.out.print(comment_num[i] + " ");
        }
        System.out.println();
    }

    /*
    List<HashMap.Entry<String, Integer>> entryList 从大到小的排序
     */
    public static void adv() throws IOException{
        String filePath = "data.json";
        String data = new String(Files.readAllBytes(Paths.get(filePath)));
        Pattern pattern = Pattern.compile("\\b(java|javax|org|com)\\.[A-Za-z0-9_.]+\\b");
        Matcher matcher = pattern.matcher(data);
        HashMap<String, Integer> map = new HashMap<>();
        while (matcher.find()) {
            String match = matcher.group();
            map.put(match, map.getOrDefault(match, 0) + 1);
        }
        List<HashMap.Entry<String, Integer>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        //打印结果、、、、、、、、、、、、、、、、、、
        for (int i = 0;i<5;i++){
            System.out.println(entryList.get(i));
        }


    }


    public static List<JSONObject> readJson(String filePath) throws IOException{
        List<JSONObject> questions = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0; i < jsonArray.length(); i++) {
            questions.add(jsonArray.getJSONObject(i));
        }
       return questions;
    }
}
