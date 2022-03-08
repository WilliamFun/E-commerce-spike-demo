package org.example.controller;

import org.example.controller.viewobject.ItemVO;
import org.example.error.BussinessException;
import org.example.response.CommonReturnType;
import org.example.service.CacheService;
import org.example.service.ItemService;
import org.example.service.model.ItemModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller("item")
@RequestMapping("/item")//在URL上的访问路径
@CrossOrigin(allowCredentials = "true",allowedHeaders = "*",originPatterns = "*")//实现跨域请求，主要session共享
public class ItemController extends BaseController{//尽可能使controller简单，service复杂

    @Autowired
    private ItemService itemService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CacheService cacheService;

    //创建商品
    @RequestMapping(value = "/create",method = {RequestMethod.POST},consumes = {CONTENT_TYPE_FORMED})//映射到http的post请求
    @ResponseBody
    public CommonReturnType createItem(@RequestParam(name = "title")String title,
                                       @RequestParam(name = "description")String description,
                                       @RequestParam(name = "price") BigDecimal price,
                                       @RequestParam(name = "stock")Integer stock,
                                       @RequestParam(name = "imgUrl")String imgUrl) throws BussinessException {
        //封装service请求用来创建商品
        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setStock(stock);
        itemModel.setImgUrl(imgUrl);

        ItemModel itemModelForReturn = itemService.createItem(itemModel);
        ItemVO itemVO = convertVOFromModel(itemModelForReturn);

        return CommonReturnType.create(itemVO);
    }

    //商品详情页浏览
    @RequestMapping(value = "/get",method = {RequestMethod.GET})//映射到http的post请求
    @ResponseBody
    public CommonReturnType getItem(@RequestParam(name = "id")Integer id){

        ItemModel itemModel = null;

        //先取本地缓存(一级缓存)
        itemModel = (ItemModel) cacheService.getFromCommonCache("item_"+id);

        if(itemModel==null){
            //根据商品的id到redis内获取(二级缓存)
            //redis获取缓存异常
            Object obj = redisTemplate.opsForValue().get("item_"+id);
            itemModel = (ItemModel) obj;

            //若redis内不存在对应的itemModel，则访问下游service
            if(itemModel==null){
                itemModel = itemService.getItemById(id);
                //设置itemModel缓存到redis内
                redisTemplate.opsForValue().set("item_"+id,itemModel);
                //设置10分钟的缓存时间（有效）
                redisTemplate.expire("item_"+id,10, TimeUnit.MINUTES);

            }
            //存入本地缓存
            cacheService.setCommonCache("item_"+id,itemModel);
        }




        ItemVO itemVO = convertVOFromModel(itemModel);

        return CommonReturnType.create(itemVO);
    }

    //商品列表浏览
    @RequestMapping(value = "/list",method = {RequestMethod.GET})//映射到http的post请求
    @ResponseBody
    public CommonReturnType listItem(){
        List<ItemModel> itemModelList = itemService.listItem();

        //使用stream api 将list内的itemModel转化为itemVO java8新特性
        List<ItemVO> itemVOList = itemModelList.stream().map(itemModel -> {
            ItemVO itemVO = convertVOFromModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);

    }


    //model到VO的转换
    private ItemVO convertVOFromModel(ItemModel itemModel){
        if(itemModel==null){
            return null;
        }
        ItemVO itemVO = new ItemVO();
        BeanUtils.copyProperties(itemModel,itemVO);
        if(itemModel.getPromoModel()!=null){
            itemVO.setPromoStatus(itemModel.getPromoModel().getStatus());
            itemVO.setPromoId(itemModel.getPromoModel().getId());
            itemVO.setPromoPrice(itemModel.getPromoModel().getPromoItemPrice());
            itemVO.setStartDate(itemModel.getPromoModel().getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")));
        }else{
            itemVO.setPromoStatus(0);
        }

        return itemVO;
    }


}
