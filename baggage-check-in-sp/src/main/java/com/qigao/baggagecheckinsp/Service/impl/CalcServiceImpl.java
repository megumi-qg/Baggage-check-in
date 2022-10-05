package com.qigao.baggagecheckinsp.Service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qigao.baggagecheckinsp.Service.CalcService;
import com.qigao.baggagecheckinsp.controller.Calc;
import com.qigao.baggagecheckinsp.entity.Form;
import com.qigao.baggagecheckinsp.entity.Result;
import com.qigao.baggagecheckinsp.mapper.CalcMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

public class CalcServiceImpl extends ServiceImpl<CalcMapper, Calc> implements CalcService {
    @Override
    public Result Calc(Form form) {
        if (form.getFlight_type() == null) {
            return Result.error(1, "航班类型不能为空");
        }
        //计算价格
        int price = calPrice(form);
        if (price == -1) {
            return Result.error(1, "托运行李不符合规范，请重新输入");
        }
        return Result.success(price);

    }
    public int calPrice(Form form) {
        String flight_type = form.getFlight_type();
        String person = form.getPerson();
        String vip = form.getVip();
        String seat_type = form.getSeat_type();
        String region = form.getRegion();
        Integer num = Integer.valueOf(form.getNum());//行李数量
        Integer []volumes=new Integer[num];
        Integer []weights=new Integer[num];
        String []types=new String[num];
        Integer price = Integer.valueOf(form.getPrice());

        String []specification1 = form.getLength1().split(" ");
        volumes[0]=Integer.parseInt(specification1[0])+Integer.parseInt(specification1[1])+Integer.parseInt(specification1[2]);
        weights[0]=Integer.valueOf(specification1[3]);
        types[0] = form.getType1();//行李1类型
        //第二件行李
        String []specification2 = form.getLength2().split(" ");
        volumes[1]=Integer.parseInt(specification2[0])+Integer.parseInt(specification2[1])+Integer.parseInt(specification2[2]);
        weights[1]=Integer.valueOf(specification2[3]);
        types[1]=form.getType2();
        //第三件行李
        String []specification3 = form.getLength3().split(" ");
        volumes[2]=Integer.parseInt(specification3[0])+Integer.parseInt(specification3[1])+Integer.parseInt(specification3[2]);
        weights[2]=Integer.valueOf(specification3[3]);
        types[2]=form.getType2();

        int TotalPrice=0;
        //首先判断是否为特殊行李,但是运动器具A、其他特殊行李A也算作普通行李
        for(int i=0;i<num;i++){
            if (!Objects.equals(types[i], "普通行李") && !Objects.equals(types[i],"运动器械器具A") && !Objects.equals(types[i],"其他特殊行李A")){
                int n=specialBaggage(types[i],weights[i]);
                if(n==-1){
                    return -1;
                }else TotalPrice+=specialBaggage(types[i],weights[i]);
            }
        }
        //航班类型
        if (flight_type.equals("国内航班")) {
            int freeWeight=freeinInland(person,vip,seat_type);
            TotalPrice+=inland(volumes,weights,types,freeWeight,price); //没有考虑行李的类型
        }
        else if (flight_type.equals("国际航班")) {//计件制 全都是23kg/32kg的
            String freeWeight=freeAbroad(person, vip, seat_type,region);//String 左右两边分别是23kg行李和32kg行李的数量
            TotalPrice+=abroad(volumes,weights,types,freeWeight,region,seat_type);
        }
        else {
            return -1;//其他航班
        }
        return TotalPrice;

    }
    // 国内计重制, 根据乘客的舱位、会员、乘客属性计算可免费运输的重量
    public int freeinInland(String person, String vip, String seat_type){
        int weight=0;
        switch (person){
            case "adult":
            case "child":
                switch(seat_type){
                    case "头等舱":weight+=40;break;
                    case "公务舱":weight+=30;break;
                    case "悦享经济舱":
                    case "超级经济舱":
                    case "经济舱": weight+=20;break;
                }
                break;
            case "baby":
                weight+=10;
                break;
        }
        switch (vip){
            case "凤凰知音终身白金卡":
            case "凤凰知音白金卡": weight+=30;break;
            case "凤凰知音金卡":
            case "凤凰知音银卡":
            case "星空联盟金卡":weight+=20;break;
        }
        return weight;
    }
    public int specialBaggage(String type1,Integer weight1){
        int price=0;
        switch (type1){
            case "可免费运输的特殊行李":
                break;
            case "B类运动器械器具":
                //重量在2~45kg
                if (weight1<2||weight1>45){
                    return -1;
                }
                else if(weight1<=23){
                    price+=2600;
                }
                else if(weight1<=32){
                    price+=3900;
                }
                else {
                    price+=5200;
                }
                break;
            case "C类运动器械器具":
                if (weight1<2||weight1>45){
                    return -1;
                }
                else if(weight1<=23){
                    price+=1300;
                }
                else if(weight1<=32){
                    price+=2600;
                }
                else {
                    price+=3900;
                }
                break;
            case "B类其他特殊行李":
                if (weight1<2||weight1>32){
                    return -1;
                }
                else if(weight1<=23){
                    price+=490;
                }
                else {
                    price+=3900;
                }
                break;
            case "C类其他特殊行李":
                if (weight1<2||weight1>32){
                    return -1;
                }
                else if(weight1<=23){
                    price+=1300;
                }
                else {
                    price+=2600;
                }
                break;
            case "D类其他特殊行李":
                if (weight1<2||weight1>5){
                    return -1;
                }
                else {
                    price+=1300;
                }
                break;
            case "E类其他特殊行李":
                if (weight1<2||weight1>32){
                    return -1;
                }
                else if(weight1<=8){
                    price+=3900;
                }
                else if (weight1<=23) {
                    price+=5200;
                }
                else {
                    price+=7800;
                }
                break;
        }
        return price;
    }
    public double inland(Integer []volumes,Integer []weights,String []types,int freeWeight,int price) {
        int num=volumes.length;
        int actualWeight=0;
        double baggageprice=0;
        for(int i=0;i<num;i++){
            if(Objects.equals(types[i], "普通行李") || Objects.equals(types[i], "A类运动器械器具") || Objects.equals(types[i], "A类其他特殊行李")){
                if(volumes[i]<60||volumes[i]>203||weights[i]<2||weights[i]>32) return -1;
                //计算各个行李的总重量
                actualWeight+=weights[i];
            }
        }
        //超出重量的部分
        if(actualWeight>freeWeight){
            baggageprice=(actualWeight-freeWeight)*price*0.015;
        }
        return baggageprice;

    }
    public String freeAbroad(String person, String vip, String seat_type,String region){
        int num32=0;
        int num23=0;
        //计算32kg行李的数量
        if(Objects.equals(person, "adult") || Objects.equals(person, "child")){
            switch(seat_type){
                case "s1":
                case "s2": num32+=2;break;
                case "s3":
                case "s4":num23+=2;break;
                case "s5":
                    if(region=="r1"||region=="r2") num23+=2;
                    else num23+=1;
                    break;
            }
        }
        else num23+=1;
        switch(vip){
            case "v0":break;
            case "v1":
            case "v2":
            case "v3":
            case "v4":
                if (Objects.equals(seat_type, "s1") || Objects.equals(seat_type, "s2")) num32+=1;
                else num23+=1;
                break;
            case "v5":
                num23+=1;
        }
        return String.valueOf(num23)+" "+String.valueOf(num32);
    }

    public double abroad(Integer []volumes,Integer []weights,String []types,String freeWeight,String region,String seat_type){
        int n=volumes.length;
        double price=0;
        String []baggage=freeWeight.split(" ");
        int num23=Integer.parseInt(baggage[0]);//对于23kg行李，超出23算超重量，
        int num32=Integer.parseInt(baggage[1]);//对于32kg的行李，没有超重量一说，但两类行李都可能超尺寸
        int leftnum=0;//额外行李数目
        int oversize=0;//158cm<S<=203cm 仅超尺寸
        int overweight1=0;//23kg<W<=28kg 仅超重量1
        int overweight2=0;//28kg<W<=32kg 仅超重量2
        int weightnsize=0;//23kg<W<=28kg & 158cm<S<=203cm 超重量且超尺寸
        //先对行李进行从大到小的排序
        //免费的行李
        //头等舱|公务舱全是32kg, 三种经济舱全是23kg
        //超出的行李要对重量体积做限制
        if(Objects.equals(seat_type, "s1") || Objects.equals(seat_type, "s2")){
            //计算超尺寸行李的数量，额外行李，不存在超重量
            for(int i=0;i<n;i++){
                if(Objects.equals(types[i], "普通行李") || Objects.equals(types[i], "A类运动器械器具") || Objects.equals(types[i], "A类其他特殊行李")) {
                    if (volumes[i] < 60 || volumes[i] > 203 || weights[i] < 2 || weights[i] > 32) return -1;
                    if (num32!=0){
                        if(volumes[i]>158){//超尺寸
                            oversize+=1;
                            num32-=1;
                        }
                        else num32-=1;
                    }
                    else if(volumes[i]>=60&&volumes[i]<=158) leftnum++;
                    else return -1;
                }
            }
        }
        else if(Objects.equals(seat_type, "s3") || Objects.equals(seat_type, "s4")||Objects.equals(seat_type, "s5")){
            //计算超尺寸行李的质量
            for(int i=0;i<n;i++){
                if(Objects.equals(types[i], "普通行李") || Objects.equals(types[i], "A类运动器械器具") || Objects.equals(types[i], "A类其他特殊行李")) {
                    if (volumes[i] < 60 || volumes[i] > 203 || weights[i] < 2 || weights[i] > 32) return -1;
                    if(num23!=0){
                        if(weights[i]>23&&weights[i]<=28&&volumes[i]>158&&volumes[i]<=203) weightnsize++;
                        else if(weights[i]<=23&&volumes[i]>158&&volumes[i]<=203) oversize++;
                        else if(weights[i]>23&&weights[i]<=28&&volumes[i]<=158) overweight1++;
                        else if(weights[i]>28&&weights[i]<=32&&volumes[i]<=158) overweight2++;
                    }
                    else if(volumes[i]>=60&&volumes[i]<=158&&weights[i]<=23) leftnum++;
                    else return -1;
                }
            }
        }
        switch(region){
            case "r1":
                price+=380*overweight1+980*overweight2+980*oversize+1400*weightnsize;
                if(leftnum==1) price+=1400;
                else if (leftnum==2) price+=1400+2000;
                else if(leftnum>=3) price+=1400+2000+3000*(leftnum-2);
                break;
            case "r2":
                price+=280*overweight1+690*(overweight2+oversize)+1100*weightnsize;
                if(leftnum==1) price+=1100;
                else if (leftnum==2) price+=1100+1100;
                else if(leftnum>=3) price+=1100+1100+1590*(leftnum-2);
                break;
            case "r3":
                price+=520*(oversize+overweight1+overweight2+weightnsize);
                if(leftnum==1) price+=1170;
                else if (leftnum==2) price+=1170+1170;
                else if(leftnum>=3) price+=1170+1170+1590*(leftnum-2);
                break;
            case "r4":
                price=690*overweight1+1040*(oversize+overweight2)+2050*weightnsize;
                if(leftnum==1) price+=1380;
                else if (leftnum==2) price+=1380+1380;
                else if(leftnum>=3) price+=1380+1380+1590*(leftnum-2);
                break;
            case "r5":
                price=210*overweight1+520*(oversize+overweight2)+830*weightnsize;
                if(leftnum==1) price+=830;
                else if (leftnum==2) price+=830+1100;
                else if(leftnum>=3) price+=830+1100+1590*(leftnum-2);
                break;
        }

        return price;
    }


}
