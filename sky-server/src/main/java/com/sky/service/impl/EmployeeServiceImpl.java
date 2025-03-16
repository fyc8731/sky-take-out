package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordEditFailedException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 对前端传过来的密码进行md5加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public Result saveEmployee(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        //将请求的数据添加到employee实体类中
        BeanUtils.copyProperties(employeeDTO,employee);
        String password = DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes());
        //设置员工初始密码
        employee.setPassword(password);
        //设置员工状态
        employee.setStatus(StatusConstant.ENABLE);
        //设置创建时间和更新时间
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
//        //设置创建人和更新人(通过线程中的局部变量获取当前登录用户的ID)
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        int insert = employeeMapper.insert(employee);
        if (insert > 0) {
            return Result.success();
        }
        return Result.error("新增员工失败");
    }

    @Override
    public Result<PageResult> employeePageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageResult pageResult=new PageResult();
        //获取请求参数中的当前页和每页记录数
        Integer page = employeePageQueryDTO.getPage();
        Integer pageSize = employeePageQueryDTO.getPageSize();
        String name = employeePageQueryDTO.getName();
        //开始分页查询
        PageHelper.startPage(page,pageSize);
        //使用分页插件获取sql语句执行后返回的结果和总记录数
        Page<Employee> employeeList= employeeMapper.getPageQuery(name);
        pageResult.setRecords(employeeList.getResult());
        pageResult.setTotal(employeeList.getTotal());
        return Result.success(pageResult);
    }

    @Override
    public Result enableOrDisableEmployee(Integer status, Long id) {
        Employee employee= Employee
                .builder()
                .id(id)
                .status(status)
                .build();

        int updateResult = employeeMapper.update(employee);
        //判断是否修改成功
        if (updateResult > 0) {
            return Result.success();
        }
        return Result.error("账号状态更新失败");
    }

    @Override
    public Result<Employee> getEmployee(Long id) {
        //调用mapper获取员工信息
        Employee employee=employeeMapper.getEmployee(id);
        employee.setPassword("****");
        return Result.success(employee);
    }

    @Override
    public Result updateEmployee(EmployeeDTO employeeDTO) {
        Employee employee=new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        //设置更新时间和更新人
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(BaseContext.getCurrentId());
        //更新员工信息，并返回更新结果
        int updateResult = employeeMapper.update(employee);
        //判断是否更新成功
        if (updateResult > 0) {
            return Result.success();
        }
        return Result.error("员工信息更新失败");
    }

    @Override
    public Result editPassword(PasswordEditDTO passwordEditDTO) {
        //获取旧密码
        String oldPassword = passwordEditDTO.getOldPassword();
        //获取新密码
        String newPassword = passwordEditDTO.getNewPassword();
        //获取当前用户的信息
        Employee employee = employeeMapper.getEmployee(BaseContext.getCurrentId());
        //判断数据库中的密码是否与旧密码相同
        if(!employee.getPassword().equals(DigestUtils.md5DigestAsHex(oldPassword.getBytes()))) {
            throw new PasswordEditFailedException(MessageConstant.PASSWORD_EDIT_FAILED);
        }
        //将新密码加密更新到数据库
        String newPasswordMd5 = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        employee.setPassword(newPasswordMd5);
        int update = employeeMapper.update(employee);
        //判断是否更新成功
        if (update > 0) {
            return Result.success();
        }
        return Result.error("密码修改失败");
    }

}
