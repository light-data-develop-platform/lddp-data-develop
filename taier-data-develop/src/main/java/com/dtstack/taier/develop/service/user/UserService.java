/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dtstack.taier.develop.service.user;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dtstack.taier.common.enums.Deleted;
import com.dtstack.taier.common.exception.TaierDefineException;
import com.dtstack.taier.common.http.PoolHttpClient;
import com.dtstack.taier.dao.domain.User;
import com.dtstack.taier.dao.dto.UserDTO;
import com.dtstack.taier.dao.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    @Value("${lddp.admin.url}")
    public String adminUrl;

    public String getUserName(Long userId) {
        User user = this.baseMapper.selectById(userId);
        return null == user ? "" : user.getUserName();
    }

    public Map<Long, User> getUserMap(Collection<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return new HashMap<>();
        }
        List<User> users = this.baseMapper.selectBatchIds(userIds);
        if (CollectionUtils.isEmpty(users)) {
            return new HashMap<>();
        }
        return users.stream().collect(Collectors.toMap(User::getId, u -> u));
    }

    public User getById(Long userId) {
        return this.baseMapper.selectById(userId);
    }


    public List<User> listAll() {
        return this.baseMapper.selectList(Wrappers.lambdaQuery(User.class).eq(User::getIsDeleted, Deleted.NORMAL.getStatus()));
    }

    public User getByUserName(String username) {
        return this.baseMapper.selectOne(Wrappers.lambdaQuery(User.class).eq(User::getUserName, username));
    }

    public UserDTO getUserByDTO(Long userId) {
        if (userId == null) {
            return null;
        }
        User one = getById(userId);
        if (Objects.isNull(one)) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(one, userDTO);
        return userDTO;
    }

    public User getUserByToken(String token, String clientId) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", "Bearer " + token);
        headers.put("clientid", clientId);
        String result = PoolHttpClient.get(adminUrl + "/system/user/getInfo", null, headers, true);
        JSONObject data = JSONObject.parseObject(result);
        return userConvert(data);
    }

    public User userConvert(JSONObject data){
        if (!Objects.equals(data.getIntValue("code"), 200)){
            throw new TaierDefineException("token校验失败");
        }
        JSONObject object= data.getJSONObject("data").getJSONObject("user");
        Long userId = object.getLongValue("userId");
        User user = getById(userId);
        String username = object.getString("userName") + "@lddp.com";
        if (user == null) {
            user = new User();
            user.setId(userId);
            user.setUserName(username);
            user.setPhoneNumber(object.getString("phonenumber"));
            user.setEmail(username);
            user.setPassword("0192023A7BBD73250516F069DF18B500");
            this.baseMapper.insert(user);
            user = getById(userId);
        }

        return user;
    }

}
