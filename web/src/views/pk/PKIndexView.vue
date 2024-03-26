<template>
        <PlayGround v-if="$store.state.pk.status === 'playing'" />
        <MatchGround v-if="$store.state.pk.status === 'matching'" />
        <ResultBoard v-if="$store.state.pk.loser !==  'none'" />
        <div class="user-color" v-if="$store.state.pk.status==='playing' &&  parseInt($store.state.user.id) === parseInt($store.state.pk.a_id)">你出生在左下角</div>
        <div class="user-color" v-if="$store.state.pk.status==='playing'&& parseInt($store.state.user.id) === parseInt($store.state.pk.b_id)">你出生在右上角</div>
</template>
<script>

 import PlayGround from "../../components/PlayGround.vue";
 import MatchGround from "../../components/MatchGround.vue";
 import ResultBoard from "../../components/ResultBoard.vue";
 import { onMounted,onUnmounted } from 'vue';
 import { useStore } from 'vuex';

export default ({
    components: {
        PlayGround,
        MatchGround,
        ResultBoard,
    },
    setup() {
        const store = useStore();
        //字符串中有${}表达式操作的话要用``，不能用引号
        const socketUrl = `wss://app2652.acapp.acwing.com.cn/websocket/${store.state.user.token}/`;
        store.commit("updateLoser", "none");
        store.commit("updateIsRecord", false);
        let socket = null;
        onMounted(() => { //当当前页面打开时调用

            store.commit("updateOpponent",{
                username: "我的对手",
                photo: "https://cdn.acwing.com/media/article/image/2022/08/09/1_1db2488f17-anonymous.png",

            })
            socket = new WebSocket(socketUrl); //js自带的WebSocket()
            socket.onopen = () => { //连接成功时调用的函数
                console.log("connected!");
                store.commit('updateSocket', socket)
            }

            socket.onmessage = msg => { //前端接收到信息时调用的函数
                const data = JSON.parse(msg.data); //不同的框架数据定义的格式不一样
                if (data.event === "start-matching") { //这个这个start-matching是respA或respB返回的
                    //匹配成功,更新对手信息
                    store.commit("updateOpponent",{
                        username: data.opponent_username,
                        photo: data.opponent_photo,
                    });
                    setTimeout(()=>{
                        store.commit("updateStatus","playing");
                    },1000)
                    store.commit("updateGame",data.game);
                }else if(data.event === "move"){
                    console.log(data.event);
                    const game = store.state.pk.gameObject;
                    const [snake0,snake1] = game.snakes;
                    console.log(data.a_direction,data.b_direction)
                    snake0.set_direction(data.a_direction);
                    snake1.set_direction(data.b_direction);
                }else if(data.event === "result"){
                    console.log(data.event);
                    const game = store.state.pk.gameObject;
                    const [snake0,snake1] = game.snakes;
                    if(data.loser === "all" || data.loser === "A"){
                        snake0.status = "die";
                    }
                    if(data.loser === "all" || data.loser === "B"){
                        snake1.status = "die";
                    }
                    store.commit("updateLoser",data.loser);
                }
            }

            socket.onclose = () => { //关闭时调用的函数
                console.log("disconnected!");
            }
        });
        onUnmounted(() => { //当当前页面关闭时调用
            store.commit("updateStatus","matching");
            socket.close(); //卸载的时候断开连接
        });
    },
})
</script>

<style scoped>
.playground {
    width: 60vw;
    height: 70vh;
    margin: 40px auto;
}
div.user-color{

    text-align: center;
    color: white;
    font-size: 30px;
    font-weight: 600;
}
</style>