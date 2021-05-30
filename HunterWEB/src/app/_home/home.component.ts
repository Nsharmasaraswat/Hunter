import { Component, ElementRef, HostListener, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { TokenService } from '../security/services/token.service';
import { fadeAnimation } from './components/custom.animations';

declare var $: any;

@Component({
  animations: [fadeAnimation],
  templateUrl: "home.component.html",
  styleUrls: ["home.component.scss"],
})
export class HomeComponent implements OnInit, OnDestroy {
  visible: boolean = true;
  permissions: any[] = [];
  windowWidth: number;
  version: string = environment.version || "unversioned";
  env: string = environment.name;
  kiosk: boolean = this.tokenSvc.isKiosk();

  constructor(private tokenSvc: TokenService, private router: Router) {}

  ngOnInit() {
    this.adjustScreen(window.innerWidth, window.innerHeight);
    if (this.kiosk) this.visible = false;

    var clicked = false;

    $("#sidebarToggle, #sidebarToggleTop").on("click", function (e) {
       clicked = true;
       $("#accordionSidebar").show();
      $("body").toggleClass("sidebar-toggled");
      $(".sidebar").toggleClass("toggled");

      if ($(".sidebar").hasClass("toggled")) {
        $(".sidebar .collapse").hide();
        $("#userPanel").css({ "font-size": "85%" });
        $("#sidebarLogo").css({ "max-width": "75px" });
        $("#sidebarFilter").hide();
      } else {
        $("#userPanel").css({ "font-size": "100%" });
        $("#sidebarLogo").css({ "max-width": "140px" });
        $("#sidebarFilter").show();
        $("#sidebarFilter").css({ width: "100%" });
      }
    });

    // Close any open menu accordions when window is resized below 768px
    $(window).resize(function () {
        if ($(window).width() < 480) {
            $("#accordionSidebar").hide()
        } else {
            $("#accordionSidebar").show()
        }
        
       if ($(window).width() > 768) {
        $("#sidebarFilter").css({ width: "100%" });
           if (clicked == false) {
                $("body").removeClass("sidebar-toggled");
                $(".sidebar").removeClass("toggled")

                $("#userPanel").css({ "font-size": "100%" });
                $("#sidebarLogo").css({ "max-width": "140px" });
                
                $("#sidebarFilter").show();
                
            }
        }
    });

    // Prevent the content wrapper from scrolling when the fixed side navigation hovered over
    $("body.fixed-nav .sidebar").on(
      "mousewheel DOMMouseScroll wheel",
      function (e) {
        if ($(window).width() > 768) {
          var e0 = e.originalEvent,
            delta = e0.wheelDelta || -e0.detail;
          this.scrollTop += (delta < 0 ? 1 : -1) * 30;
          e.preventDefault();
        }
      }
    );

    // Scroll to top button appear
    $(document).on("scroll", function () {
      var scrollDistance = $(this).scrollTop();
      if (scrollDistance > 100) {
        $(".scroll-to-top").fadeIn();
      } else {
        $(".scroll-to-top").fadeOut();
      }
    });

    // Smooth scrolling using jQuery easing
    $(document).on("click", "a.scroll-to-top", function (e) {
      var $anchor = $(this);
      $("html, body")
        .stop()
        .animate(
          {
            scrollTop: $($anchor.attr("href")).offset().top,
          },
          1000,
          "easeInOutExpo"
        );
      e.preventDefault();
    });
  }

  ngOnDestroy() {
    if (this.kiosk) {
      this.logout();
    }
  }
  @HostListener("window:beforeunload", ["$event"])
  beforeUnloadHander(event) {
    if (this.kiosk) {
      this.logout();
    }
  }

  logout() {
    this.tokenSvc.logout();
    this.router.navigate(["/security/login"]);
  }

  @HostListener("window:resize", ["$event"])
  onResize(event) {
    this.adjustScreen(event.target.innerWidth, event.target.innerHeight);
  }

  adjustScreen(width: number, height: number): void {
    let actualVH = height * 0.01;

    this.windowWidth = width;
    document.documentElement.style.setProperty("--actualVH", `${actualVH}px`);
    this.checkMenuVisibility();
  }

  checkMenuVisibility() {
    if (this.windowWidth < 1024) this.visible = false;
    else this.visible = true;
  }

  sideBar() {
    this.visible = !this.visible;
  }

  home() {
    this.router.navigate(["/home"]);
  }

  public getRouterOutletState(outlet) {
    return outlet.isActivated ? outlet.activatedRoute : "";
  }

 
}
