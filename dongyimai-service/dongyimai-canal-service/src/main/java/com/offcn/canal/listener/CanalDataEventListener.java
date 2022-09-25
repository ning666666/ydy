package com.offcn.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.offcn.item.feign.PageFeign;
import com.xpand.starter.canal.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@CanalEventListener //项目识别监听的注解
public class CanalDataEventListener {
    @Autowired
    private PageFeign pageFeign;

    @ListenPoint(destination = "example",
            schema = "dongyimaidb",
            table = {"tb_goods"},
            eventType = {CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT, CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        //判断操作类型
        if (eventType == CanalEntry.EventType.DELETE) {
            String goodsId = "";
            List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
            for (CanalEntry.Column column : beforeColumnsList) {
                if (column.getName().equals("id")) {
                    goodsId = column.getValue();//goodsId
                    break;
                }
            }
            //todo 删除静态页

        }else{
            //新增 或者 更新
            List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
            String goodsId = "";
            for (CanalEntry.Column column : afterColumnsList) {
                if (column.getName().equals("id")) {
                    goodsId = column.getValue();
                    break;
                }
            }
            //更新 生成静态页
            pageFeign.createHtml(Long.valueOf(goodsId));
        }
    }
/*    *//***
     * 增加数据监听
     * @param eventType 当前要操作的类型 新增eventType==insert
     * @param rowData 监听数据改变的前后信息
     *//*
    @InsertListenPoint //监听新增
    public void onEventInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
      *//*for(CannalEntry.Column c: rowData.getAfterColumnsList()){
          System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
      }*//*
    }

    *//***
     * 修改数据监听
     * @param rowData
     *//*
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.RowData rowData) {
        System.out.println("UpdateListenPoint");
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    *//***
     * 删除数据监听
     * @param eventType
     *//*
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EventType eventType) {
        System.out.println("DeleteListenPoint");
    }
    //查询不需要监听
    *//***
     * 自定义数据修改监听
     * @param eventType
     * @param rowData
     * 对应下面四个参数：固定、监听哪个库、监听哪个表、监听某个操作
     *//*
    @ListenPoint(destination = "example", schema = "dongyimaidb", table = {"tb_content_category", "tb_content"}, eventType = CanalEntry.EventType.UPDATE)
    public void onEventCustomUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        System.err.println("ListenPoint");
        rowData.getAfterColumnsList().forEach((c) -> System.out.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }*/
}
