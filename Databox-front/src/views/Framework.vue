<template>
  <div class="framework">
    <div class="header">
      <AppTitle2 title="DataBox" />
      <div class="search-container">
        <Search v-model="searchKeyword" @search="handleSearch" />
        <el-popover v-model:visible="showUploader" trigger="click" :width="800" :offset="20" transition="el-zoom-in-top"
          :hide-after="1" :popper-style="{ padding: '0px', maxHeight: '600px', overflow: 'hidden' }">
          <template #reference>
            <span class="iconfont icon-transfer"></span>
          </template>
          <template #default>
            <Uploader ref="uploaderRef" @uploadCallback="uploadCallbackHandler"></Uploader>
          </template>
        </el-popover>
      </div>
      <div class="right-panel">
        <div class="user-info">
          <div class="avatar">
            <Avatar :userId="userInfo.userId" :userName="userInfo.userName" :userEmail="userInfo.email"
              :timestamp="timestamp" @action="handleAvatarAction" />
          </div>
        </div>
      </div>
    </div>
    <div class="body">
      <div class="left-sider">
        <div class="menu-list">
          <div :class="['menu-item', item.menuCode == currentMenu.menuCode ? 'active' : '']" v-for="item in filteredMenus"
            @click="jump(item)">
            <div class="icon-container">
              <div :class="['iconfont', 'icon-' + item.icon]"></div>
            </div>
            <div class="text">{{ item.name }}</div>
            <div class="active-indicator" v-if="item.menuCode == currentMenu.menuCode"></div>
          </div>
        </div>
        <div class="menu-sub-list">
          <div :class="['menu-item-sub', currentPath == sub.path ? 'active' : '']" v-for="sub in currentMenu.children"
            @click="jump(sub)">
            <span :class="['iconfont', 'icon-' + sub.icon]" v-if="sub.icon"></span>
            <span class="text">{{ sub.name }}</span>
          </div>
          <div class="tips" v-if="currentMenu && currentMenu.tips">
            {{ currentMenu.tips }}
          </div>
          <div class="space-info">
            <div class="space-title">
              <i class="iconfont icon-cloude"></i>
              <span>空间使用</span>
            </div>
            <div class="percent">
              <el-progress :percentage="Math.floor(useSpaceInfo.useSpace / useSpaceInfo.totalSpace * 10000) / 100"
                :color="spaceProgressColor" :stroke-width="8" :show-text="false">
              </el-progress>
            </div>
            <div class="space-use">
              <div class="use">
                <span class="used-space">{{ proxy.Utils.size2Str(useSpaceInfo.useSpace) }}</span>
                <span class="space-separator">/</span>
                <span class="total-space">{{ proxy.Utils.size2Str(useSpaceInfo.totalSpace) }}</span>
              </div>
              <div class="refresh-btn" @click="getUserSpace" title="刷新空间信息">
                <i class="iconfont icon-refresh"></i>
              </div>
            </div>
            <div class="space-percentage">
              已使用 {{ Math.floor(useSpaceInfo.useSpace / useSpaceInfo.totalSpace * 10000) / 100 }}%
            </div>
          </div>
        </div>
      </div>
      <div class="body-content">
        <router-view v-slot="{ Component }">
          <component :is="Component" @addFile="addFile" ref="routerViewRef" @reload="getUserSpace"></component>
        </router-view>
      </div>
    </div>
    <UpdateAvatar ref="uploadAvatarRef" @UpdateAvatar="reloadUserAvatar"></UpdateAvatar>
    <UpdatePassword ref="updatePasswordRef" />
    <Logout ref="logoutRef" />
  </div>
</template>

<script setup>
import Search from '@/components/Input/Search.vue';
import Uploader from '@/views/main/Uploader.vue';
import Logout from '@/components/Logout.vue';
import UpdatePassword from './UpdatePassword.vue';
import UpdateAvatar from './UpdateAvatar.vue';
import AppTitle2 from '@/components/AppTitle/AppTitle2.vue';
import { ref, reactive, getCurrentInstance, nextTick, watch, computed } from "vue"
const { proxy } = getCurrentInstance();
import { useRoute, useRouter } from 'vue-router';
const route = useRoute();
const router = useRouter();

const userInfo = ref(proxy.VueCookies.get('userInfo'))

const api = {
  getUserSpace: '/getUseSpace'
}

// 根据使用空间百分比动态设置进度条颜色
const spaceProgressColor = computed(() => {
  const percentage = Math.floor(useSpaceInfo.value.useSpace / useSpaceInfo.value.totalSpace * 10000) / 100;
  if (percentage < 50) {
    return '#05a1f5'; // 绿色 - 使用量低
  } else if (percentage < 80) {
    return '#E6A23C'; // 黄色 - 使用量中等
  } else {
    return '#F56C6C'; // 红色 - 使用量高
  }
});

// 菜单
const menus = [
  {
    icon: 'cloude',
    name: '首页',
    menuCode: 'main',
    path: '/main/all',
    allShow: true,
    children: [
      {
        icon: 'all',
        name: "全部",
        category: 'all',
        path: '/main/all'
      },
      {
        icon: 'video',
        name: "视频",
        category: 'video',
        path: '/main/video'
      },
      {
        icon: 'image',
        name: "图片",
        category: 'image',
        path: '/main/image'
      },
      {
        icon: 'music',
        name: "音乐",
        category: 'music',
        path: '/main/music'
      },
      {
        icon: 'doc',
        name: "文档",
        category: 'doc',
        path: '/main/doc'
      },
      {
        icon: "more",
        name: "其他",
        category: 'others',
        path: '/main/others'
      }
    ]
  },
  {
    path: '/myshare',
    icon: 'share',
    name: '分享',
    menuCode: 'share',
    allShow: true,
    children: [
      {
        name: '分享记录',
        path: '/myshare'
      }
    ]
  },
  {
    path: '/recycle',
    icon: 'del',
    name: '回收站',
    menuCode: 'recycle',
    allShow: true,
    tips: '回收站保存10天内删除的文件',
    children: [
      {
        name: '已删除文件',
        path: '/recycle'
      }
    ]
  },
  {
    path: '/settings/fileList',
    icon: 'settings',
    name: '设置',
    menuCode: 'settings',
    allShow: true,
    children: [
      {
        name: '用户文件',
        path: '/settings/fileList'
      },
      {
        name: '用户管理',
        path: '/settings/userList'
      },
      {
        name: '系统设置',
        path: '/settings/sysSetting'
      }
    ]
  }
]
// 根据用户权限过滤菜单
const filteredMenus = computed(() => {
  return menus.filter(item => {
    // 如果不是设置菜单，或者用户是管理员，则显示
    return item.menuCode !== 'settings' || userInfo.value.isAdmin;
  });
});



const timestamp = ref(0)
const currentMenu = ref({})
const currentPath = ref()
// 路由跳转
const jump = (data) => {
  if (!data.path || data.menuCode == currentMenu.value.menuCode) {
    return
  }
  router.push(data.path)
}

// 选中菜单
const setMenu = (menuCode, path) => {
  const menu = menus.find(item => {
    return item.menuCode == menuCode
  })
  // 如果是设置菜单但用户不是管理员，则不设置当前菜单
  if (menu && menu.menuCode === 'settings' && !userInfo.value.isAdmin) {
    router.push('/main/all'); // 重定向到首页
    return;
  }
  currentMenu.value = menu
  currentPath.value = path
}
// 路由监听
watch(() => route, (newVal, oldVal) => {
  if (newVal.meta.menuCode) {
    setMenu(newVal.meta.menuCode, newVal.path)
  }
}, { immediate: true, deep: true });

// 上传头像
const uploadAvatarRef = ref()
const updateAvatar = () => {
  uploadAvatarRef.value.show(userInfo.value)
}

// 修改密码
const updatePasswordRef = ref()
const updatePassword = () => {
  updatePasswordRef.value.show()
}

// 退出登录
const logoutRef = ref()
// 处理 Avatar 组件操作的方法
const handleAvatarAction = (action) => {
  if (action === 'editAvatar') {
    updateAvatar()
    console.log('修改头像');
  } else if (action === 'changePassword') {
    // 处理修改密码
    updatePassword()
    console.log('修改密码');
  } else if (action === 'logout') {
    // 处理退出登录
    logoutRef.value.show()
  }
};

// 重新加载用户头像
const reloadUserAvatar = (data) => {
  userInfo.value = proxy.VueCookies.get('userInfo');
  timestamp.value = new Date().getTime();
}

// 显示文件上传popover
const showUploader = ref(false)
const uploaderRef = ref()
// 上传文件
const addFile = (data) => {
  const { file, filePid } = data;
  showUploader.value = true;
  uploaderRef.value.addFile(file, filePid);
}

// 上传文件回调
const routerViewRef = ref()
const uploadCallbackHandler = () => {
  nextTick(() => {
    routerViewRef.value.reload()
    // 更新用户空间
    getUserSpace();
  })
}

// 搜索关键词
const searchKeyword = ref('');
// 处理搜索事件
const handleSearch = (keyword) => {
  searchKeyword.value = keyword;
  // 如果当前不在首页，跳转到首页
  if (!route.path.startsWith('/main')) {
    router.push('/main/all');
  }
  // 通知子组件进行搜索
  nextTick(() => {
    if (routerViewRef.value && routerViewRef.value.searchFiles) {
      routerViewRef.value.searchFiles(keyword);
    }
  });
}

// 用户空间
const useSpaceInfo = ref({
  useSpace: 0,
  totalSpace: 1
})

// 获取用户空间信息
const getUserSpace = async () => {
  let result = await proxy.Request({
    url: api.getUserSpace,
    showLoading: false,
  })
  if (!result) {
    return;
  }
  useSpaceInfo.value = result.data;
}
getUserSpace();
</script>

<style lang="scss" scoped>
.framework {
  background: #c3cfe2;
  // background: -webkit-linear-gradient(to right, #c3cfe2, #f5f7fa);
  // background: linear-gradient(to right, #c3cfe2, #f5f7fa);
  min-height: 100vh;
  user-select: none;
}

.header {
  box-shadow: 0 3px 10px 0 rgb(0 0 0 / 20%);
  height: 60px;
  padding-left: 24px;
  padding-right: 24px;
  position: relative;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .search-container {
    flex: 1;
    display: flex;
    justify-content: center;
    margin: 0 20px;

    .icon-transfer {
      cursor: pointer;
      margin-left: 15px;
      font-size: 16px; // 增加字体大小
      color: #05a1f5; // 改为蓝色
      padding: 10px;
      border-radius: 50%;
      background-color: rgba(5, 161, 245, 0.1); // 添加淡蓝色背景
      transition: all 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;

      &:hover {
        color: #ffffff; // 悬停时文字变白色
        background-color: rgba(5, 161, 245, 0.8); // 悬停时背景变深蓝
        transform: scale(1.05);
      }

      &.active {
        color: #ffffff;
        background-color: #05a1f5;
        box-shadow: 0 2px 8px rgba(5, 161, 245, 0.3);
      }
    }
  }

  .logo {
    display: flex;
    align-items: center;

    .icon-pan {
      font-size: 40px;
      color: #129ddb
    }

    .name {
      font-weight: bold;
      margin-left: 5px;
      font-size: 25px;
      color: #05a1f5;
    }
  }

  .right-panel {
    display: flex;
    align-items: center;

    .user-info {
      margin-right: 80px;
      /* 增加右边距，将头像向左移 */
      margin-left: 15px;
      /* 添加左边距，增加与上传图标的间距 */
      display: flex;
      align-items: center;
      cursor: pointer;

      .avatar {
        margin: 0px 5px 0px 15px;
      }

      .user-name {
        color: #2980b9;
        font-weight: 600;
        font-size: 20px;
        transition: all 0.3s ease;
        padding: 4px 8px;
        border-radius: 4px;

        &:hover {
          background-color: rgba(41, 128, 185, 0.1);
          color: #1565C0;
        }
      }
    }
  }
}

.body {
  display: flex;
  height: calc(100vh - 60px);
  overflow: hidden;

  .left-sider {
    border-right: 1px solid rgba(27, 26, 26, 0.11);
    display: flex;

    .menu-list {
      height: calc(100vh - 56px);
      width: 80px;
      box-shadow: 0 3px 10px 0 rgb(0 0 0 / 10%);
      border-right: 1px solid #bfc9d8;

      .menu-item {
        position: relative;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        font-size: 14px;
        font-weight: 500;
        padding: 18px 0;
        cursor: pointer;
        transition: all 0.3s ease;
        border-radius: 10px;
        margin: 2px 6px;
        overflow: hidden;

        &::after {
          content: '';
          position: absolute;
          top: 50%;
          left: 50%;
          width: 5px;
          height: 5px;
          background: rgba(5, 161, 245, 0.3);
          opacity: 0;
          border-radius: 100%;
          transform: scale(1, 1) translate(-50%);
          transform-origin: 50% 50%;
        }

        &:active::after {
          animation: ripple 0.6s ease-out;
        }

        &:hover {
          background: rgba(243, 243, 243, 0.8);
          box-shadow: 0 2px 8px rgba(5, 161, 245, 0.5);
          /* 添加轻微阴影效果 */
        }

        .icon-container {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 40px;
          height: 40px;
          border-radius: 10px;
          margin-bottom: 4px;
          transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        }

        .iconfont {
          font-weight: normal;
          font-size: 24px;
          color: #7a8ca0;
          transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        }

        .text {
          font-size: 13px;
          color: #7a8ca0;
          transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        }

        .active-indicator {
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 0px;
          background: #05a1f5;
          border-radius: 0 0px 0px 0;
          transition: height 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
        }
      }

      .active {
        background: rgba(255, 255, 255, 0.459);
        transform: translateY(-2px);

        .icon-container {
          background: rgba(5, 161, 245, 0.15);
          transform: scale(1.05);
        }

        .iconfont {
          color: #05a1f5;
        }

        .text {
          color: #05a1f5;
          font-weight: 600;
        }

        .active-indicator {
          height: 25px;
          /* 激活时展开到完整高度 */
        }
      }
    }

    .menu-sub-list {
      width: 200px;
      padding: 20px 10px 0px;
      position: relative;

      .menu-item-sub {
        text-align: center;
        line-height: 42px;
        /* 增加行高 */
        border-radius: 8px;
        /* 增加圆角 */
        cursor: pointer;
        margin: 4px 0;
        /* 添加上下间距 */
        transition: all 0.3s ease;
        /* 添加过渡效果 */
        font-size: 14px;
        /* 增加字体大小 */

        &:hover {
          background: rgba(255, 255, 255, 0.568);
          box-shadow: 0 2px 8px rgba(5, 161, 245, 0.336);
        }

        .iconfont {
          font-size: 15px;
          margin-right: 20px;
          transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        }

        .text {
          font-size: 14px;
          color: #5a6a7a;
          font-weight: 400;
          letter-spacing: 0.5px;
          transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
          position: relative;

          &::after {
            content: '';
            position: absolute;
            bottom: -2px;
            left: 0;
            width: 0;
            height: 2px;
            background-color: #05a1f5;
            transition: width 0.3s ease;
            opacity: 0.8;
          }
        }

        &:hover .text {
          color: #05a1f5;
          /* 悬停时文字变为主题色 */
          transform: translateX(2px);
          /* 轻微右移效果 */

          &::after {
            width: 100%;
            /* 悬停时显示下划线 */
          }
        }
      }

      .active {
        background: rgba(255, 255, 255, 0.568);
        box-shadow: 0 2px 8px rgba(5, 161, 245, 0.336);

        .iconfont {
          color: #05a1f5;
        }

        .text {
          color: #05a1f5;
          font-weight: 600;
          transform: translateX(2px);
          text-shadow: 0 0 1px rgba(5, 161, 245, 0.2);

          &::after {
            width: 100%;
          }
        }
      }

      .tips {
        margin-top: 10px;
        color: #888888;
        font-size: 13px;
      }

      .space-info {
        position: absolute;
        bottom: 10px;
        width: 185px;
        padding: 10px;
        background-color: rgba(255, 255, 255, 0);
        border-radius: 10px;
        //box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        // transition: all 0.3s ease;

        // &:hover {
        //   transform: translateY(-2px);
        //   box-shadow: 0 4px 16px 0 rgba(0, 0, 0, 0.15);
        // }

        .space-title {
          display: flex;
          align-items: center;
          margin-bottom: 10px;
          font-weight: 600;
          color: #333;

          i {
            margin-right: 8px;
            font-size: 18px;
            color: #05a1f5;
          }
        }

        .percent {
          padding-right: 10px;
          margin-bottom: 8px;
        }

        .space-use {
          margin-top: 5px;
          display: flex;
          justify-content: space-between;
          align-items: center;

          .use {
            flex: 1;
            font-size: 14px;
            color: #606266;

            .used-space {
              font-weight: 600;
              color: #05a1f5;
            }

            .space-separator {
              margin: 0 4px;
              color: #909399;
            }

            .total-space {
              color: #909399;
            }
          }

          .refresh-btn {
            cursor: pointer;
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background-color: #f0f2f5;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s ease;
            position: relative;
            overflow: hidden;

            &:hover {
              background-color: #05a1f5;
              transform: rotate(90deg); // 旋转效果

              i {
                color: white;
              }
            }

            &:active {
              transform: scale(0.85) rotate(90deg); // 点击时缩小效果
              background-color: #0483c9; // 点击时颜色变深
            }

            &::after {
              content: '';
              position: absolute;
              top: 50%;
              left: 50%;
              width: 5px;
              height: 5px;
              background: rgba(255, 255, 255, 0.7);
              opacity: 0;
              border-radius: 100%;
              transform: scale(1, 1) translate(-50%, -50%);
              transform-origin: 50% 50%;
            }

            &:active::after {
              animation: ripple 0.6s ease-out;
            }

            i {
              font-size: 16px;
              color: #05a1f5;
              transition: all 0.3s ease;
            }
          }

          @keyframes ripple {
            0% {
              transform: scale(0, 0);
              opacity: 0.5;
            }

            20% {
              transform: scale(25, 25);
              opacity: 0.5;
            }

            100% {
              opacity: 0;
              transform: scale(40, 40);
            }
          }
        }

        .space-percentage {
          font-size: 13px;
          color: #909399;
          text-align: center;
          margin-top: 5px;
        }
      }
    }
  }

  .body-content {
    flex: 1;
    width: 0;
    padding-left: 20px;
  }
}
</style>
