package com.qigao.baggagecheckinsp.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qigao.baggagecheckinsp.controller.Calc;
import com.qigao.baggagecheckinsp.entity.Form;
import com.qigao.baggagecheckinsp.entity.Result;

public interface CalcService extends IService<Calc> {
    public Result Calc(Form form);
}
