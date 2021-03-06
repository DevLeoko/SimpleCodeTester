<template>
  <nav>
    <v-toolbar dark color="primary darken-1">
      <v-toolbar-side-icon v-if="!actionsHidden" @click="drawer = !drawer"></v-toolbar-side-icon>
      <v-toolbar-title class="d-flex flex-centered">
        {{ title }}
        <a
          v-if="!actionsHidden"
          href="https://github.com/I-Al-Istannen/SimpleCodeTester"
          class="pl-4 py-0"
          style="line-height: 0px;"
          target="_blank"
          rel="noopener"
        >
          <img :src="require('@/assets/Github_Mark.png')" width="32" height="32">
        </a>
      </v-toolbar-title>

      <v-spacer></v-spacer>
      <div v-if="!actionsHidden" class="hidden-sm-and-down">
        <!-- PROFILE -->
        <v-btn flat v-if="currentRoute.name !== 'profile'" @click="$router.push('/profile')">Profile
          <v-icon right dark>person</v-icon>
        </v-btn>

        <!-- CHECK CODE -->
        <v-btn
          flat
          v-if="currentRoute.name !== 'checkCode'"
          @click="$router.push('/check-code')"
        >Check code
          <v-icon right dark>star_half</v-icon>
        </v-btn>

        <!-- LOGOUT -->
        <v-btn flat @click="logout">Logout
          <v-icon right dark>exit_to_app</v-icon>
        </v-btn>
      </div>
    </v-toolbar>

    <!-- DRAWER -->
    <v-navigation-drawer v-model="drawer" app temporary v-if="!actionsHidden">
      <v-toolbar dark color="primary darken-1">
        <v-list>
          <v-list-tile>
            <v-list-tile-title class="title">Navigation</v-list-tile-title>
          </v-list-tile>
        </v-list>
      </v-toolbar>

      <v-divider></v-divider>

      <v-list dense class="pt-0">
        <v-list-tile
          v-for="item in applicableItems"
          :key="item.title"
          @click="navigate(item)"
          :class="{selected: item.predicatePath === currentRoute.name, headline: true}"
        >
          <v-list-tile-action>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-tile-action>

          <v-list-tile-content>
            <v-list-tile-title>{{ item.title }}</v-list-tile-title>
          </v-list-tile-content>
        </v-list-tile>
      </v-list>
    </v-navigation-drawer>
  </nav>
</template>

<script lang="ts">
import Vue from "vue";
import { Store } from "vuex";
import { RootState } from "@/store/types";
import { Route } from "vue-router";
import Component from "vue-class-component";
import { Prop } from "vue-property-decorator";
import { user } from "@/store/modules/user";

@Component({})
export default class NavigationBar extends Vue {
  title = "Simple Code Tester";
  drawer = false;

  navigationItems = [
    {
      icon: "person",
      title: "Profile",
      route: "/profile",
      predicatePath: "profile"
    },
    {
      icon: "star_half",
      title: "Check Code",
      route: "/check-code",
      predicatePath: "checkCode"
    },
    {
      icon: "history",
      title: "View last check result",
      route: "/view-check-result",
      predicatePath: "viewCheckResult",
      statefulIf: function(state: RootState): boolean {
        return state.checkresult.checkResult !== null;
      }
    },
    {
      icon: "add_circle_outline",
      title: "Submit check",
      route: "/submit-check",
      predicatePath: "submitCheck"
    },
    {
      icon: "list",
      title: "All checks",
      route: "/view-checks",
      predicatePath: "viewChecks"
    },
    {
      icon: "category",
      title: "Manage check categories",
      route: "/view-check-categories",
      predicatePath: "viewCheckCategories"
    },
    {
      icon: "people",
      title: "Manage Users",
      route: "/view-users",
      predicatePath: "viewUsers",
      admin: true
    },
    {
      icon: "lock",
      title: "Change password",
      route: "/change-own-password",
      predicatePath: "changePassword"
    },
    {
      icon: "exit_to_app",
      title: "Logout",
      route: "/login",
      predicatePath: "login"
    }
  ];

  @Prop()
  actionsHidden!: Boolean;

  get currentRoute(): Route {
    return this.$route;
  }

  get applicableItems(): Array<any> {
    return this.navigationItems.filter(it => {
      // only for admins and you are none
      if (it.admin && (it.admin && !this.isAdmin)) {
        return false;
      }
      // does not have further restrictions
      if (!it.statefulIf) {
        return true;
      }
      return it.statefulIf(this.$store.state);
    });
  }

  get isAdmin(): boolean {
    const userState = (this.$store as Store<RootState>).state.user;
    return userState.roles && userState.roles.some(it => it === "ROLE_ADMIN");
  }

  logout() {
    this.$store.dispatch("logout");
    this.$router.push("/login");
  }

  navigate(item: any) {
    if (item.title === "Logout") {
      this.logout();
    } else {
      this.$router.push(item.route);
    }
  }
}
</script>

<style scoped>
#nav-list {
  background-color: var(--primary-darken-1);
}

#main-title {
  font-size: 20px;
  font-weight: 500;
}

.selected {
  background-color: #e8e8e8;
}
</style>

<style>
.v-list__tile {
  font-size: 15px !important;
}
.flex-centered {
  justify-content: center;
  align-items: center;
}
</style>
