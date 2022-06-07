package com.melvin.ongandroid.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.melvin.ongandroid.R
import com.melvin.ongandroid.databinding.FragmentHomeBinding
import com.melvin.ongandroid.model.data.news.NewsList
import com.melvin.ongandroid.model.data.slides.SlidesList
import com.melvin.ongandroid.model.network.ApiStatus
import com.melvin.ongandroid.model.data.testimonials.TestimonialsList
import com.melvin.ongandroid.view.adapters.NewsViewPagerAdapter
import com.melvin.ongandroid.view.adapters.SlidesAdapter
import com.melvin.ongandroid.view.adapters.TestimonialsAdapter
import com.melvin.ongandroid.viewmodel.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<HomeViewModel>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)


        viewModel.getSlides()
        viewModel.getTestimonials()
        viewModel.getNews()

        setupStatusLiveDataMerger(viewModel)
        
        viewModel.homeStatusLiveDataMerger.observe(viewLifecycleOwner, Observer {
            if (it == ApiStatus.LOADING){homeIsLoading(true,binding)}
            else if (it == ApiStatus.DONE){homeIsLoading(false,binding)}
            else {
                when (viewModel.messageCombineHomeStatusData(viewModel.slidesStatus,viewModel.newsStatus,viewModel.testimonialsStatus)){
                    2 -> showErrorDialog2(viewModel)
                    3 -> showErrorDialog3(viewModel)
                    4 -> showErrorDialog4(viewModel)
                    5 -> showErrorDialog5(viewModel)
                }
            }
        })

        viewModel.slidesList.observe(viewLifecycleOwner, Observer {
            setSlides(viewModel,binding) //Load slides
        })

        viewModel.testimonialsList.observe(viewLifecycleOwner, Observer {
            setTestimonials(viewModel, binding) //Load testimonials
        })


        viewModel.newsList.observe(viewLifecycleOwner, Observer {
            when (it) {
                is State.Success -> setNews(it.data)
                is State.Failure -> showErrorDialog(callback = { viewModel.getNews() })
                is State.Loading -> showSpinnerLoading(true)
            }
        })

        return binding.root

    }


    override fun onDestroyView() {
        super.onDestroyView()
        onDestroyNews()
    }
    
    private fun setSlides(viewModel: HomeViewModel, binding: FragmentHomeBinding) {
        val slidesList = viewModel.slidesList.value

        if (slidesList != null && slidesList.success && !slidesList.slide.isNullOrEmpty()) {
            binding.rvSlides.adapter = SlidesAdapter(slidesList.slide)
        }

    }


    private fun setNews(viewModel: HomeViewModel, binding: FragmentHomeBinding) {
        val newsList = viewModel.newsList.value

            if (newsList != null && newsList.success && !newsList.data.isNullOrEmpty()) {
                //Initialize news adapter
                binding.vpNews.adapter = NewsViewPagerAdapter(newsList.data)
                //Set starting page for news viewpager
                val currentPageIndex = 0
                binding.vpNews.currentItem = currentPageIndex
                //Registering for page change callback
                binding.vpNews.registerOnPageChangeCallback(
                    object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                        }
                    }
                )
        }
    }

    private fun setTestimonials(viewModel: HomeViewModel, binding: FragmentHomeBinding) {
        val testimonialsList = viewModel.testimonialsList.value


            if ( testimonialsList != null && testimonialsList.success && !testimonialsList.testimonials.isNullOrEmpty()) {
                binding.rvTestimony.adapter =
                    TestimonialsAdapter(testimonialsList.testimonials, true)
        }
    }

    private fun onDestroyNews() {
        val viewpager = view?.findViewById<ViewPager2>(R.id.vp_news)
        //Unregistering the onPageChangedCallback
        viewpager?.unregisterOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {}
        )
    }

    private fun setupStatusLiveDataMerger(viewModel: HomeViewModel){

        viewModel.homeStatusLiveDataMerger.addSource(viewModel.slidesStatus, Observer {
            viewModel.homeStatusLiveDataMerger.value = viewModel.combineHomeStatusData(viewModel.slidesStatus,viewModel.newsStatus,viewModel.testimonialsStatus)
        })

        viewModel.homeStatusLiveDataMerger.addSource(viewModel.newsStatus, Observer {
            viewModel.homeStatusLiveDataMerger.value = viewModel.combineHomeStatusData(viewModel.slidesStatus,viewModel.newsStatus,viewModel.testimonialsStatus)
        })

        viewModel.homeStatusLiveDataMerger.addSource(viewModel.testimonialsStatus, Observer {
            viewModel.homeStatusLiveDataMerger.value = viewModel.combineHomeStatusData(viewModel.slidesStatus,viewModel.newsStatus,viewModel.testimonialsStatus)
        })
    }

    private fun homeIsLoading(loading:Boolean, binding: FragmentHomeBinding){
        if(loading){
            binding.progressBar1.visibility = View.VISIBLE
            binding.rvSlides.visibility = View.GONE
            binding.btnContact.visibility = View.GONE
            binding.tvNews.visibility = View.GONE
            binding.vpNews.visibility = View.GONE
            binding.btnWantToJoin.visibility = View.GONE
            binding.tvTestimonyTitle.visibility = View.GONE
            binding.rvTestimony.visibility = View.GONE
            binding.btnAddMyTestimonial.visibility = View.GONE
        }
        else{
            binding.progressBar1.visibility = View.GONE
            binding.rvSlides.visibility = View.VISIBLE
            binding.btnContact.visibility = View.VISIBLE
            binding.tvNews.visibility = View.VISIBLE
            binding.vpNews.visibility = View.VISIBLE
            binding.btnWantToJoin.visibility = View.VISIBLE
            binding.tvTestimonyTitle.visibility = View.VISIBLE
            binding.rvTestimony.visibility = View.VISIBLE
            binding.btnAddMyTestimonial.visibility = View.VISIBLE
        }
    }
    private fun showErrorDialog2(viewModel: HomeViewModel){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage("Inicio - Error general")
            .setPositiveButton("Reintentar") { _, _ -> viewModel.updateHome() }
            .show()
    }

    private fun showErrorDialog3(viewModel: HomeViewModel){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage("Error al cargar slides")
            .setPositiveButton("Reintentar") { _, _ -> viewModel.getSlides() }
            .show()
    }

    private fun showErrorDialog4(viewModel: HomeViewModel){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage("Error al cargar novedades")
            .setPositiveButton("Reintentar") { _, _ -> viewModel.getNews() }
            .show()
    }

    private fun showErrorDialog5(viewModel: HomeViewModel){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage("Error al cargar testimonios")
            .setPositiveButton("Reintentar") { _, _ -> viewModel.getTestimonials() }
            .show()
    }
}