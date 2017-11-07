# cardgame
卡牌游戏

## 2017/11/7 19:54
###修改CardInfoActivity代码
1.将每个按钮事件改写成对应class
2.删除update事件里enqueue函数里面new Thread操作，因为本身就是子线程没有必要
3.黄清，你每个activity结束了都没finish,你看看，你自己又创建了一个activity没关，但是我不知道在哪儿操作
4.在服务器端添加了deleteCardServlet和UpdateCardServlet;