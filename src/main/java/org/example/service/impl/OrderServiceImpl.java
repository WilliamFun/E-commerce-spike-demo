package org.example.service.impl;

import org.example.dao.OrderDOMapper;
import org.example.dao.SequenceDOMapper;
import org.example.dataobject.OrderDO;
import org.example.dataobject.SequenceDO;
import org.example.error.BussinessException;
import org.example.error.EmBusinessError;
import org.example.service.ItemService;
import org.example.service.OrderService;
import org.example.service.UserService;
import org.example.service.model.ItemModel;
import org.example.service.model.OrderModel;
import org.example.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional//保证创建订单在同一个事务当中
public class OrderServiceImpl implements OrderService {
    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    public OrderModel createOrder(Integer userId, Integer itemId, Integer amount) throws BussinessException {
        //1、校验下单状态，下单的商品是否存在，用户是否合法，购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if(itemModel==null){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"商品信息不存在");
        }
        UserModel userModel = userService.getUserById(userId);
        if(userModel==null){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"用户信息不存在");
        }
        if(amount<=0||amount>99){
            throw new BussinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR,"数量信息不正确");
        }
        //2、落单减库存（或支付减库存）
        boolean result = itemService.decreaseStock(itemId,amount);
        if(!result){
            throw new BussinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        //3、订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setUserId(userId);
        orderModel.setItemId(itemId);
        orderModel.setAmount(amount);
        orderModel.setItemPrice(itemModel.getPrice());
        orderModel.setOrderPrice(itemModel.getPrice().multiply(new BigDecimal(amount)));

        //生成订单号
        orderModel.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //加上商品的销量
        itemService.increaseSales(itemId,amount);
        //4、返回前端
        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)//在事务中开启一个新的事务，这段代码运行结束后直接提交
    String generateOrderNo(){
        //订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        //前8位时间信息，年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-","");
        stringBuilder.append(nowDate);
        //中间6位为自增序列
        //获取当前Sequence
        int sequence;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue()+sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for(int i = 0;i<6-sequenceStr.length();i++){//可能比6位还要大
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);


        //最后2位为分库分表位（均匀落在n个表内，分散落单压力）,暂时写死
        stringBuilder.append("00");
        return stringBuilder.toString();

    }
    private OrderDO convertFromOrderModel(OrderModel orderModel){
        if(orderModel==null){
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel,orderDO);
        orderDO.setItemPrice(orderModel.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderModel.getOrderPrice().doubleValue());
        return orderDO;
    }
}
