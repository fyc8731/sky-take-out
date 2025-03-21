package com.sky.controller.admin;

import com.github.pagehelper.Page;
import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@Api(value = "/admin/employee", tags = {"员工管理"})
@RestController
@RequestMapping("/admin/employee")
@Slf4j
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "EmployeeLoginDTO", name = "employeeLoginDTO", value = "", required = true)
    })
    @ApiOperation(value = "登录", notes = "登录", httpMethod = "POST")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @ApiOperation(value = "退出", notes = "退出", httpMethod = "POST")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增用户
     * @param employeeDTO
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "EmployeeDTO", name = "employeeDTO", value = "", required = true)
    })
    @ApiOperation(value = "新增用户", notes = "新增用户", httpMethod = "POST")
    @PostMapping
    public Result saveEmployee(@RequestBody EmployeeDTO employeeDTO) {
        return employeeService.saveEmployee(employeeDTO);
    }

    /**
     * 分页显示员工信息表
     * @return
     */
    @ApiOperation(value = "分页显示员工信息表", notes = "分页显示员工信息表", httpMethod = "GET")
    @GetMapping("/page")
    public Result<PageResult> PageQueryEmployee(EmployeePageQueryDTO employeePageQueryDTO) {
        return employeeService.employeePageQuery(employeePageQueryDTO);
    }

    /**
     * 员工账号启用/禁用功能
     * @param status
     * @param id
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "int", name = "status", value = "", required = true)
    })
    @ApiOperation(value = "员工账号启用/禁用功能", notes = "员工账号启用/禁用功能", httpMethod = "POST")
    @PostMapping("/status/{status}")
    public Result enableOrDisableEmployee(@PathVariable("status") Integer status,Long id) {
        return employeeService.enableOrDisableEmployee(status,id);
    }

    /**
     * 根据id回显员工信息
     * @param id
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "path", dataType = "long", name = "id", value = "", required = true)
    })
    @ApiOperation(value = "修改回显员工信息", notes = "修改回显员工信息", httpMethod = "GET")
    @GetMapping("/{id}")
    public Result<Employee> getEmployee(@PathVariable("id") Long id) {
        return employeeService.getEmployee(id);
    }

    /**
     * 修改员工信息功能
     * @param employeeDTO
     * @return
     */

    @PutMapping
    public Result updateEmployee(@RequestBody EmployeeDTO employeeDTO) {
        return employeeService.updateEmployee(employeeDTO);
    }

    /**
     * 密码修改功能
     * @param passwordEditDTO
     * @return
     */
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", dataType = "PasswordEditDTO", name = "passwordEditDTO", value = "", required = true)
    })
    @ApiOperation(value = "密码修改功能", notes = "密码修改功能", httpMethod = "PUT")
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        return employeeService.editPassword(passwordEditDTO);
    }
}
