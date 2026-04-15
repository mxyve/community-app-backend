package top.xym.community.app.module.service.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.xym.community.app.module.service.model.entity.PayServiceOrder;

public interface PayServiceOrderMapper extends BaseMapper<PayServiceOrder> {

    /**
     * 根据订单号更新支付状态为已支付
     */
    @Update("UPDATE pay_service_order SET pay_status = 1, pay_time = NOW() WHERE order_no = #{orderNo}")
    int updatePayStatus(@Param("orderNo") String orderNo);

    /**
     * 根据订单号查询支付状态
     */
    @Select("SELECT pay_status FROM pay_service_order WHERE order_no = #{orderNo}")
    Integer getPayStatus(@Param("orderNo") String orderNo);
}