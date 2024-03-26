<template>
    <ContentField v-if="!$store.state.user.is_login">
        <div class="row justify-content-md-center">
            <div class="col-3">
                <form @submit.prevent="login">
                    <div class="mb-3">
                        <label for="username" class="form-label">用戶名</label>
                        <input v-model="username" type="text" class="form-control" id="username" placeholder="用戶名">
                    </div>
                    <div class="mb-3">
                        <label for="password" class="form-label">密码</label>
                        <input v-model="password" type="password" class="form-control" id="password" placeholder="密码">
                    </div>
                    <div class="error-msg">{{ error_message }}</div>
                    <button type="submit" class="btn btn-primary">提交</button>
                </form>
                <div style="text-align: center; margin-top: 20px; cursor: pointer;" @click="acwing_login">
                    <img width="30" src="https://cdn.acwing.com/media/article/image/2022/09/06/1_32f001fd2d-acwing_logo.png" alt="">
                    <br>
                    AcWing一键登录
                </div>
            </div>
        </div>
    </ContentField>
</template>
<script>

import ContentField from "../../../components/ContentField.vue";
import { useStore } from 'vuex';
import {ref} from 'vue';
import router from '../../../router/index';
import $ from 'jquery'


export default ({
    components: {
        ContentField,
    },
    setup() {
        const store = useStore();
        let username = ref('');
        let password = ref('');
        let error_message = ref('');
        const login = () => {
            error_message.value = "";

            store.dispatch("login",{
                username: username.value,
                password: password.value,
                success(){
                    store.dispatch('getinfo',{
                        success(){
                            router.push({ name: 'home' });
                            console.log(store.state.user);
                        }
                    })
                },
                error(){
                    error_message.value= "用户名或者密码错误";
                }
            })

        }
        const acwing_login = () => {
            $.ajax({
                url:"https://app2652.acapp.acwing.com.cn/api/user/account/acwing/web/apply_code/",
                type:"get",
                success: resp =>{
                    if(resp.result==="success"){
                        window.location.replace(resp.apply_code_url);
                    }
                }
            })
        }
        return {
            username,
            password,
            error_message,
            login,
            acwing_login,
        }
    },
})
</script>

<style scoped>
button{
    width: 100%;
}
div.error-msg{
    color: red;
}
</style>
